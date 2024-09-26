package net.kapitencraft.scripted.lang.oop;

import net.kapitencraft.scripted.lang.env.core.Environment;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;
import net.kapitencraft.scripted.lang.run.Interpreter;

public abstract class LoxField {

    public abstract Object initialize(Environment environment, Interpreter interpreter);

    public abstract LoxClass getType();
}
