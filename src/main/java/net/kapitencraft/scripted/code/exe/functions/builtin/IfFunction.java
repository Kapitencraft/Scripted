package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.AppendFunction;
import net.kapitencraft.scripted.code.exe.functions.abstracts.AppendableFunction;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;

import java.util.ArrayList;
import java.util.List;

public class IfFunction extends AppendableFunction<IfFunction.Instance<?>> {

    @Override
    public AppendableFunction<Instance<?>>.AppendableInstance loadInstance(JsonObject object, VarAnalyser analyser) {
        Method<Boolean>.Instance condition = Method.loadFromSubObject(object, "condition", analyser);
        MethodPipeline<?> body = MethodPipeline.load(object, analyser, false);
        return new Instance<>(condition, body);
    }

    public class Instance<T> extends AppendableFunction<IfFunction.Instance<?>>.AppendableInstance {
        private final Method<Boolean>.Instance condition;
        private final MethodPipeline<T> body;
        private ElseFunction.Instance<T> elseFunc;
        private final List<ElIfFunction.Instance<T>> elseIfs = new ArrayList<>();

        public Instance(Method<Boolean>.Instance condition, MethodPipeline<T> body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public boolean append(AppendFunction<Instance<?>>.AppendInstance func) {
            if (func instanceof ElseFunction.Instance<?> inst && this.elseFunc == null) {
                this.elseFunc = (ElseFunction.Instance<T>) inst;
                return true;
            } else if (func instanceof ElIfFunction.Instance<?> inst) {
                this.elseIfs.add((ElIfFunction.Instance<T>) inst);
                return true;
            }
            return false;
        }

        @Override
        public void save(JsonObject object) {
            object.add("condition", condition.toJson());
            object.add("body", body.toJson());
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            if (condition.callInit(map).getValue()) {
                body.execute(map, (MethodPipeline<T>) source);
                return;
            }
            if (this.elseIfs.stream().anyMatch(tInstance -> tInstance.attemptedExecute(map, source))) {
                    return;
            }
            if (this.elseFunc != null) this.elseFunc.execute(map, source);
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            this.body.analyse(analyser);
            this.elseIfs.forEach(tInstance -> tInstance.analyse(analyser));
            this.elseFunc.analyse(analyser);
        }
    }
}
