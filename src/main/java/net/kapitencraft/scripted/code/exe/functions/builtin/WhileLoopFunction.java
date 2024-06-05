package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;
import net.minecraft.util.GsonHelper;

public class WhileLoopFunction extends Function {

    @Override
    public Function.Instance load(JsonObject object, VarAnalyser analyser) {
        Method<Boolean>.Instance method = Method.loadFromSubObject(object, "condition", analyser);
        MethodPipeline<?> pipeline = MethodPipeline.load(GsonHelper.getAsJsonObject(object, "body"), analyser, true);
        return new Instance<>(method, pipeline);
    }

    public class Instance<T> extends Function.Instance {
        private final Method<Boolean>.Instance condition;
        private final MethodPipeline<T> body;

        public Instance(Method<Boolean>.Instance condition, MethodPipeline<T> body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public void save(JsonObject object) {
            object.add("condition", condition.toJson());
            object.add("body", body.toJson());
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            while (condition.callInit(map).getValue() && !source.isBrokenOrCanceled()) {
                body.execute(map, (MethodPipeline<T>) source);
            }
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            this.body.analyse(analyser);
        }
    }
}