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
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;

public class ForLoopFunction extends Function {

    public ForLoopFunction() {
        super(ParamSet.empty(), "for");
    }

    public static Method<Void>.Instance create(Method<?>.Instance start, Method<Boolean>.Instance condition, Method<?>.Instance iteration, MethodPipeline<?> pipeline) {
        return ModFunctions.FOR_LOOP.get().createInst(start, condition, iteration, pipeline);
    }

    private Method<Void>.Instance createInst(Method<?>.Instance start, Method<Boolean>.Instance condition, Method<?>.Instance iteration, MethodPipeline<?> pipeline) {
        return new Instance<>(start, condition, iteration, pipeline);
    }

    @Override
    public Instance<?> load(JsonObject object, VarAnalyser analyser) {
        Method<?>.Instance onInit = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "onInit"), analyser);
        Method<Boolean>.Instance condition = Method.loadFromSubObject(object, "condition", analyser);
        Method<?>.Instance onLoop = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "onLoop"), analyser);
        MethodPipeline<?> body = MethodPipeline.load(GsonHelper.getAsJsonObject(object, "body"), analyser, true);
        return new Instance<>(onInit, condition, onLoop, body);
    }

    @Override
    public Method<Void>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return load(object, analyser);
    }

    public class Instance<T> extends Function.Instance {
        /**
         * normally a {@link net.kapitencraft.scripted.init.ModFunctions#CREATE_AND_SET_VAR ModFunctions.CREATE_AND_SET_VAR} call
         */
        private final Method<?>.Instance onInit;
        private final Method<Boolean>.Instance condition;
        private final Method<?>.Instance onLoop;
        private final MethodPipeline<T> body;

        public Instance(Method<?>.Instance onInit, Method<Boolean>.Instance condition, Method<?>.Instance onLoop, MethodPipeline<T> body) {
            super(null);
            this.onInit = onInit;
            this.condition = condition;
            this.onLoop = onLoop;
            this.body = body;
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add("onInit", onInit.toJson());
            object.add("condition", condition.toJson());
            object.add("onLoop", onLoop.toJson());
            object.add("body", body.toJson());
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            map.push(); //fun fact, for-loops are not only good for iteration
            for (onInit.invoke(map, source); condition.callInit(map); onLoop.invoke(map, source)) {
                body.execute(map, (MethodPipeline<T>) source);
            }
            map.pop();
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            analyser.push(); //you don't want to know what's going on here...
            onInit.analyse(analyser);
            condition.analyse(analyser);
            body.analyse(analyser);
            onLoop.analyse(analyser);
            analyser.pop();
        }
    }
}
