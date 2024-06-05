package net.kapitencraft.scripted.code.exe.functions.abstracts;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;

public abstract class Function {

    public abstract Instance load(JsonObject object, VarAnalyser analyser);

    public abstract class Instance {

        public abstract void save(JsonObject object);

        public abstract void execute(VarMap map, MethodPipeline<?> source);

        public void analyse(VarAnalyser analyser) {}

        public Function getFunction() {
            return Function.this;
        }
    }
}
