package net.kapitencraft.scripted.code.exe.functions.abstracts;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.Runnable;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

import java.util.function.Consumer;

public abstract class Function extends Runnable {

    protected Function(Consumer<ParamSet> paramBuilder) {
        super(builder(paramBuilder));
    }

    private static ParamSet builder(Consumer<ParamSet> paramBuilder) {
        ParamSet set = ParamSet.function();
        paramBuilder.accept(set);
        return set;
    }

    public abstract Instance load(JsonObject object, VarAnalyser analyser);

    public abstract Instance createFromCode(String params, VarAnalyser analyser);

    public abstract class Instance extends Runnable.Instance {

        protected Instance(ParamData paramData) {
            super(paramData);
        }

        public void analyse(VarAnalyser analyser) {}

        public Function getFunction() {
            return Function.this;
        }
    }
}
