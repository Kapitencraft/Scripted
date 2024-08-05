package net.kapitencraft.scripted.code.exe.functions.abstracts;

import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

import java.util.function.Consumer;

public abstract class SpecialFunction extends Function {

    protected SpecialFunction(String name, Consumer<ParamSet> paramBuilder) {
        super(paramBuilder, name);
    }

    public abstract Instance create(String in, VarAnalyser analyser);
}
