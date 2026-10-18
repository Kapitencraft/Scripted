package net.kapitencraft.scripted.edit.graphical.holder;

import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.text.StmtParser;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.clazz.ClassConstructor;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;

public record BlocklyClassHolder(ClassReference target, AnnotationObj[] annotations, ) implements ClassConstructor {

    @Override
    public Compiler.ClassBuilder construct(StmtParser javaStmtParser, VarTypeContainer parser, ErrorStorage logger) {
        return null;
    }

    @Override
    public ScriptedClass createSkeleton(ErrorStorage logger) {
        return null;
    }

    @Override
    public Token name() {
        return null;
    }

    @Override
    public String pck() {
        return "";
    }

    @Override
    public void validate(ErrorStorage logger) {

    }
}
