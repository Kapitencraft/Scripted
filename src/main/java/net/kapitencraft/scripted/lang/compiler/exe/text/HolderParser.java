package net.kapitencraft.scripted.lang.compiler.exe.text;

import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.holder.oop.clazz.ClassConstructor;
import net.kapitencraft.scripted.lang.holder.token.Token;

public interface HolderParser {

    void apply(Token[] tokens, VarTypeContainer varTypeContainer);

    ClassConstructor parseFile(String fileName, String pck);
}
