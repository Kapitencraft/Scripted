package net.kapitencraft.scripted.code.method.elements.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.method.Method;
import net.kapitencraft.scripted.code.method.MethodPipeline;
import net.kapitencraft.scripted.code.method.elements.abstracts.Function;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;

public class ForLoopFunction extends Function {

    @Override
    public Instance<?> load(JsonObject object, VarAnalyser analyser) {
        Function.Instance onInit = JsonHelper.readFunction(GsonHelper.getAsJsonObject(object, "onInit"), analyser);
        Method<Boolean> condition = Method.loadFromSubObject(object, "condition", analyser);
        Function.Instance onLoop = JsonHelper.readFunction(GsonHelper.getAsJsonObject(object, "onLoop"), analyser);
        MethodPipeline<?> body = MethodPipeline.load(GsonHelper.getAsJsonObject(object, "body"), analyser, true);
        return new Instance<>(onInit, condition, onLoop, body);
    }

    public class Instance<T> extends Function.Instance {
        /**
         * normally a {@link net.kapitencraft.scripted.init.ModFunctions#CREATE_AND_SET_VAR ModFunctions.CREATE_AND_SET_VAR} call
         */
        private final Function.Instance onInit;
        private final Method<Boolean> condition;
        private final Function.Instance onLoop;
        private final MethodPipeline<T> body;

        public Instance(Function.Instance onInit, Method<Boolean> condition, Function.Instance onLoop, MethodPipeline<T> body) {
            this.onInit = onInit;
            this.condition = condition;
            this.onLoop = onLoop;
            this.body = body;
        }

        @Override
        public void save(JsonObject object) {
            object.add("onInit", JsonHelper.saveFunction(onInit));
            object.add("condition", condition.toJson());
            object.add("onLoop", JsonHelper.saveFunction(onLoop));
            object.add("body", body.toJson());
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            map.push(); //fun fact, for-loops are not only good for iteration
            for (onInit.execute(map, source); condition.call(map).getValue(); onLoop.execute(map, source)) {
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
