package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.AppendFunction;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;

public class ElseFunction extends AppendFunction<IfFunction.Instance<?>> {
    @Override
    public Instance<?> load(JsonObject object, VarAnalyser analyser) {
        return null;
    }

    public class Instance<T> extends ElseFunction.AppendInstance {
        private final MethodPipeline<T> body;

        public Instance(MethodPipeline<T> body) {
            this.body = body;
        }

        @Override
        public void save(JsonObject object) {
            object.add("body", body.toJson());
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            body.execute(map, (MethodPipeline<T>) source);
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            body.analyse(analyser);
        }
    }
}
