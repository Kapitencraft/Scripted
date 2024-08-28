package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IfFunction extends Function {

    @Override
    public MethodInstance<Void> load(JsonObject object, VarAnalyser analyser) {
        return loadInst(object, analyser);
    }

    private <T> Instance<T> loadInst(JsonObject main, VarAnalyser analyser) {
        MethodPipeline<T> pipeline = MethodPipeline.load(GsonHelper.getAsJsonObject(main, "body"), analyser, false);
        MethodInstance<Boolean> condition = Method.loadInstance(GsonHelper.getAsJsonObject(main, "condition"), analyser);
        List<Pair<MethodInstance<Boolean>, MethodPipeline<T>>> elifs = new ArrayList<>();
        if (main.has("elifs")) JsonHelper
                .castToObjects(GsonHelper.getAsJsonArray(main, "elifs"))
                .map(object -> {
                    MethodInstance<Boolean> conditionInst = Method.loadInstance(GsonHelper.getAsJsonObject(object, "method"), analyser);
                    MethodPipeline<T> body = MethodPipeline.load(GsonHelper.getAsJsonObject(object, "body"), analyser, false);
                    return Pair.of(conditionInst, body);
                })
                .forEach(elifs::add);
        MethodPipeline<T> elsePipeline = main.has("else") ? MethodPipeline.load(GsonHelper.getAsJsonObject(main, "else"), analyser, false) : null;
        return new Instance<>(condition, pipeline, elifs, elsePipeline);
    }

    public <T> Instance<T> createInst(Pair<MethodInstance<Boolean>, MethodPipeline<T>> main, List<Pair<MethodInstance<Boolean>, MethodPipeline<T>>> elifs, MethodPipeline<T> elseBody, VarAnalyser analyser) {
        return new Instance<>(main.getFirst(), main.getSecond(), elifs, elseBody);
    }

    public class Instance<T> extends Function.Instance {
        private final MethodPipeline<T> body;
        private final MethodInstance<Boolean> condition;
        private final List<Pair<MethodInstance<Boolean>, MethodPipeline<T>>> elifs;
        private final @Nullable MethodPipeline<T> elseBody;

        public Instance(MethodInstance<Boolean> condition, MethodPipeline<T> body, List<Pair<MethodInstance<Boolean>, MethodPipeline<T>>> elifs, @Nullable MethodPipeline<T> elseBody) {
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
            for (Pair<MethodInstance<Boolean>, MethodPipeline<T>> pair : elifs) {
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
            for (Pair<MethodInstance<Boolean>, MethodPipeline<T>> pair : elifs) {
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
