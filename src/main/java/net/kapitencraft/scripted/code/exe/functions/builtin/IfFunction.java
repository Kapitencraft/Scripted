package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IfFunction extends Function {

    @Override
    public Method<Void>.Instance load(JsonObject object, VarAnalyser analyser) {
        return loadInst(object, analyser);
    }

    private <T> Instance<T> loadInst(JsonObject main, VarAnalyser analyser) {
        MethodPipeline<T> pipeline = MethodPipeline.load(GsonHelper.getAsJsonObject(main, "body"), analyser, false);
        Method<Boolean>.Instance condition = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(main, "condition"), analyser);
        List<Pair<Method<Boolean>.Instance, MethodPipeline<T>>> elifs = new ArrayList<>();
        if (main.has("elifs")) JsonHelper
                .castToObjects(GsonHelper.getAsJsonArray(main, "elifs"))
                .map(object -> {
                    Method<Boolean>.Instance conditionInst = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "method"), analyser);
                    MethodPipeline<T> body = MethodPipeline.load(GsonHelper.getAsJsonObject(object, "body"), analyser, false);
                    return Pair.of(conditionInst, body);
                })
                .forEach(elifs::add);
        MethodPipeline<T> elsePipeline = main.has("else") ? MethodPipeline.load(GsonHelper.getAsJsonObject(main, "else"), analyser, false) : null;
        return new Instance<>(condition, pipeline, elifs, elsePipeline);
    }

    public <T> Instance<T> createInst(Pair<Method<Boolean>.Instance, MethodPipeline<T>> main, List<Pair<Method<Boolean>.Instance, MethodPipeline<T>>> elifs, MethodPipeline<T> elseBody, VarAnalyser analyser) {
        return new Instance<>(main.getFirst(), main.getSecond(), elifs, elseBody);
    }

    public class Instance<T> extends Function.Instance {
        private final MethodPipeline<T> body;
        private final Method<Boolean>.Instance condition;
        private final List<Pair<Method<Boolean>.Instance, MethodPipeline<T>>> elifs;
        private final @Nullable MethodPipeline<T> elseBody;

        public Instance(Method<Boolean>.Instance condition, MethodPipeline<T> body, List<Pair<Method<Boolean>.Instance, MethodPipeline<T>>> elifs, @Nullable MethodPipeline<T> elseBody) {
            this.condition = condition;
            this.body = body;
            this.elifs = elifs;
            this.elseBody = elseBody;
        }

        @Override
        public void execute(VarMap origin, MethodPipeline<?> source) {
            if (condition.call(origin, source)) {
                this.body.execute(source.getMap(), (MethodPipeline<T>) source);
                return;
            }
            for (Pair<Method<Boolean>.Instance, MethodPipeline<T>> pair : elifs) {
                if (pair.getFirst().call(origin, source)) {
                    pair.getSecond().execute(origin, (MethodPipeline<T>) source);
                    return;
                }
            }
            if (elseBody != null) elseBody.execute(origin, (MethodPipeline<T>) source);
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add("body", body.toJson());
            JsonArray array = new JsonArray();
            for (Pair<Method<Boolean>.Instance, MethodPipeline<T>> pair : elifs) {
                JsonObject pairObj = new JsonObject();
                pairObj.add("method", pair.getFirst().toJson());
                pairObj.add("body", pair.getSecond().toJson());
                array.add(pairObj);
            }
            object.add("elifs", array);
            if (elseBody != null) {
                object.add("else", elseBody.toJson());
            }
        }
    }
}
