package net.kapitencraft.scripted.code.method.elements.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.method.MethodPipeline;
import net.kapitencraft.scripted.code.method.elements.abstracts.Function;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;

public class BreakFunction extends Function {

    @Override
    public Function.Instance load(JsonObject object, VarAnalyser analyser) {
        return new Instance();
    }

    public class Instance extends Function.Instance {

        @Override
        public void save(JsonObject object) {
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            source.setBroken();
        }
    }
}
