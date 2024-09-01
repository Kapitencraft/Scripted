package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

public class ContinueFunction extends Function {

    @Override
    public Instance load(JsonObject object, VarAnalyser analyser) {
        return new Instance();
    }

    public class Instance extends Function.Instance {
        public Instance() {
            super("continue");
        }

        @Override
        protected void execute(VarMap map, MethodPipeline<?> pipeline) {
            pipeline.setContinued();
        }
    }
}
