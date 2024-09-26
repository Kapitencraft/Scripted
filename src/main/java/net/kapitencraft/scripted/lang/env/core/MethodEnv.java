package net.kapitencraft.scripted.lang.env.core;

import net.kapitencraft.scripted.lang.env.abst.Leveled;
import net.kapitencraft.scripted.lang.func.LoxCallable;

public class MethodEnv extends Leveled<String, LoxCallable> {

    MethodEnv() {
    }

    public void define(String name, LoxCallable func) {
        this.addValue(name, func);
    }

    public LoxCallable get(String name) {
        return this.getValue(name);
    }
}
