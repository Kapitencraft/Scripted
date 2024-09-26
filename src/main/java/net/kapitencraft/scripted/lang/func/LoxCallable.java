package net.kapitencraft.scripted.lang.func;

import net.kapitencraft.scripted.lang.env.core.Environment;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;
import net.kapitencraft.scripted.lang.run.Interpreter;

import java.util.List;

public interface LoxCallable {
    int arity();

    LoxClass type();

    List<? extends LoxClass> argTypes();

    Object call(Environment environment, Interpreter interpreter, List<Object> arguments);

    boolean isAbstract();
}