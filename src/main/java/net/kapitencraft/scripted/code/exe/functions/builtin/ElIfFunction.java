package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.AppendFunction;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;

public class ElIfFunction extends AppendFunction<IfFunction.Instance<?>> {

    @Override
    public Instance<?> load(JsonObject object, VarAnalyser analyser) {
        Method<Boolean>.Instance condition = Method.loadFromSubObject(object, "condition", analyser);
        MethodPipeline<?> body = MethodPipeline.load(object, analyser, false);
        return new Instance<>(condition, body);
    }

    public class Instance<I> extends ElIfFunction.AppendInstance {
        private final Method<Boolean>.Instance condition;
        private final MethodPipeline<I> body;

        public Instance(Method<Boolean>.Instance condition, MethodPipeline<I> body) {
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
            if (condition.callInit(map).getValue()) {
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