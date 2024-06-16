package net.kapitencraft.scripted.code.exe.functions.abstracts;

import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

public abstract class SpecialFunction extends Function {

    @Override
    public final Instance createFromCode(String params, VarAnalyser analyser) {
        return null;
    }

    public abstract Instance create(String in, VarAnalyser analyser);

    /**
     * @param string the statement inserted
     * @return a boolean if the string is an instance of this Function
     * <br> beware: methods are compiled later; they are part of the param
     */
    public abstract boolean isInstance(String string);
}
