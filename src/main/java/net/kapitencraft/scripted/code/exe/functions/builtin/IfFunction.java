package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.VarTypes;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IfFunction extends Function {

    public IfFunction() {
        super(set -> set.addEntry(entry -> entry.addParam("condition", VarTypes.BOOL)), "if");
    }

    @Override
    public Method<Void>.Instance load(JsonObject object, VarAnalyser analyser) {
        return loadInst(object, analyser, ParamData.of(object, analyser, this.set()));
    }

    @Override
    public Method<Void>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return loadInst(object, analyser, data);
    }

    private <T> Instance<T> loadInst(JsonObject main, VarAnalyser analyser, ParamData data) {
        MethodPipeline<T> pipeline = MethodPipeline.load(GsonHelper.getAsJsonObject(main, "body"), analyser, false);
        List<Pair<Method<Boolean>.Instance, MethodPipeline<T>>> elifs = new ArrayList<>();
        if (main.has("elifs")) JsonHelper
                .castToObjects(GsonHelper.getAsJsonArray(main, "elifs"))
                .map(object -> {
                    Method<Boolean>.Instance condition = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "method"), analyser);
                    MethodPipeline<T> body = MethodPipeline.load(GsonHelper.getAsJsonObject(object, "body"), analyser, false);
                    return Pair.of(condition, body);
                })
                .forEach(elifs::add);
        MethodPipeline<T> elsePipeline = main.has("else") ? MethodPipeline.load(GsonHelper.getAsJsonObject(main, "else"), analyser, false) : null;
        return new Instance<>(data, pipeline, elifs, elsePipeline);
    }

    public <T> Instance<T> createInst(Pair<Method<Boolean>.Instance, MethodPipeline<T>> main, List<Pair<Method<Boolean>.Instance, MethodPipeline<T>>> elifs, MethodPipeline<T> elseBody, VarAnalyser analyser) {
        return new Instance<>(ParamData.create(set(), analyser, List.of(main.getFirst())), main.getSecond(), elifs, elseBody);
    }

    public class Instance<T> extends Function.Instance {
        private final MethodPipeline<T> body;
        private final List<Pair<Method<Boolean>.Instance, MethodPipeline<T>>> elifs;
        private final @Nullable MethodPipeline<T> elseBody;

        public Instance(ParamData data, MethodPipeline<T> body, List<Pair<Method<Boolean>.Instance, MethodPipeline<T>>> elifs, @Nullable MethodPipeline<T> elseBody) {
            super(data);
            this.body = body;
            this.elifs = elifs;
            this.elseBody = elseBody;
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            if (map.getVarValue("condition", VarTypes.BOOL)) {
                this.body.execute(source.getMap(), (MethodPipeline<T>) source);
                return;
            }
            for (Pair<Method<Boolean>.Instance, MethodPipeline<T>> pair : elifs) {
                if (pair.getFirst().callInit(source.getMap())) {
                    pair.getSecond().execute(source.getMap(), (MethodPipeline<T>) source);
                    return;
                }
            }
            if (elseBody != null) elseBody.execute(source.getMap(), (MethodPipeline<T>) source);
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
