package net.kapitencraft.scripted.code.method.elements.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.method.Method;
import net.kapitencraft.scripted.code.method.MethodPipeline;
import net.kapitencraft.scripted.code.method.elements.abstracts.AppendFunction;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;

public class ElIfFunction extends AppendFunction<IfFunction.Instance<?>> {

    @Override
    public Instance<?> load(JsonObject object, VarAnalyser analyser) {
        Method<Boolean> condition = Method.loadFromSubObject(object, "condition", analyser);
        MethodPipeline<?> body = MethodPipeline.load(object, analyser, false);
        return new Instance<>(condition, body);
    }

    public class Instance<I> extends ElIfFunction.AppendInstance {
        private final Method<Boolean> condition;
        private final MethodPipeline<I> body;

        public Instance(Method<Boolean> condition, MethodPipeline<I> body) {
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
        }

        public boolean attemptedExecute(VarMap map, MethodPipeline<?> source) {
            if (condition.call(map).getValue()) {
                body.execute(map, (MethodPipeline<I>) source);
                return true;
            }
            return false;
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            this.body.analyse(analyser);
        }
    }
}