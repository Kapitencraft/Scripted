package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.ModFunctions;
import net.minecraft.util.GsonHelper;

public class ForLoopFunction extends Function {

    public static MethodInstance<Void> create(MethodInstance<?> start, MethodInstance<Boolean> condition, MethodInstance<?> iteration, MethodPipeline<?> pipeline) {
        return ModFunctions.FOR_LOOP.get().createInst(start, condition, iteration, pipeline);
    }

    private MethodInstance<Void> createInst(MethodInstance<?> start, MethodInstance<Boolean> condition, MethodInstance<?> iteration, MethodPipeline<?> pipeline) {
        return new Instance<>(start, condition, iteration, pipeline);
    }

    @Override
    public Instance<?> load(JsonObject object, VarAnalyser analyser) {
        MethodInstance<?> onInit = Method.loadInstance(object, "onInit", analyser);
        MethodInstance<Boolean> condition = Method.loadFromSubObject(object, "condition", analyser);
        MethodInstance<?> onLoop = Method.loadInstance(object, "onLoop", analyser);
        MethodPipeline<?> body = MethodPipeline.load(GsonHelper.getAsJsonObject(object, "body"), analyser, true);
        return new Instance<>(onInit, condition, onLoop, body);
    }

    public class Instance<T> extends Function.Instance {
        /**
         * normally a {@link net.kapitencraft.scripted.init.ModFunctions#CREATE_AND_SET_VAR ModFunctions.CREATE_AND_SET_VAR} call
         */
        private final MethodInstance<?> onInit;
        private final MethodInstance<Boolean> condition;
        private final MethodInstance<?> onLoop;
        private final MethodPipeline<T> body;

        public Instance(MethodInstance<?> onInit, MethodInstance<Boolean> condition, MethodInstance<?> onLoop, MethodPipeline<T> body) {
            super("forLoop");
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
        public void execute(VarMap origin, MethodPipeline<?> pipeline) {
            origin.push(); //fun fact, for-loops are not only good for iteration
            for (onInit.call(origin, pipeline); condition.call(origin, pipeline); onLoop.call(origin, pipeline)) {
                body.execute(origin, (MethodPipeline<T>) pipeline);
            }
            origin.pop();
        }

        public void analyse(VarAnalyser analyser) {
            analyser.push(); //you don't want to know what's going on here...
//            onInit.analyse(analyser);
//            condition.analyse(analyser);
//            body.analyse(analyser);
//            onLoop.analyse(analyser);
//            analyser.pop();
        }
    }
}
