package net.kapitencraft.scripted.code.exe.functions.abstracts;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.IExecutable;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

public abstract class Function {

    public abstract Instance load(JsonObject object, VarAnalyser analyser);

    public abstract Instance createFromCode(String params, VarAnalyser analyser);

    public abstract class Instance implements IExecutable {

        public abstract void save(JsonObject object);

        public void analyse(VarAnalyser analyser) {}

        public Function getFunction() {
            return Function.this;
        }
    }
}
