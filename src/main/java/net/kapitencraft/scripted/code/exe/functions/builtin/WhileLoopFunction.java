package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.ModFunctions;
import net.minecraft.util.GsonHelper;

public class WhileLoopFunction extends Function {

    public WhileLoopFunction() {
        super( ParamSet.empty(),"while");
    }

    public static Method<Void>.Instance create(Method<Boolean>.Instance condition, MethodPipeline<?> pipeline) {
        return ModFunctions.WHILE.get().createInst(condition, pipeline);
    }

    private Method<Void>.Instance createInst(Method<Boolean>.Instance condition, MethodPipeline<?> pipeline) {
        return new Instance<>(condition, pipeline);
    }

    @Override
    public Function.Instance load(JsonObject object, VarAnalyser analyser) {
        Method<Boolean>.Instance method = Method.loadFromSubObject(object, "condition", analyser);
        MethodPipeline<?> pipeline = MethodPipeline.load(GsonHelper.getAsJsonObject(object, "body"), analyser, true);
        return new Instance<>(method, pipeline);
    }

    @Override
    public Method<Void>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return load(object, analyser);
    }

    public class Instance<T> extends Function.Instance {
        private final Method<Boolean>.Instance condition;
        private final MethodPipeline<T> body;

        public Instance(Method<Boolean>.Instance condition, MethodPipeline<T> body) {
            super(ParamData.empty());
            this.condition = condition;
            this.body = body;
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add("condition", condition.toJson());
            object.add("body", body.toJson());
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            while (condition.callInit(map) && !body.isBrokenOrCanceled()) {
                body.execute(map, (MethodPipeline<T>) source);
            }
            body.reset();
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            this.body.analyse(analyser);
        }
    }
}