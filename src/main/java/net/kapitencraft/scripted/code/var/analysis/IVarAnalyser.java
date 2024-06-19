package net.kapitencraft.scripted.code.var.analysis;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

public interface IVarAnalyser {

    <T> VarType<T> getType(String name);
}
