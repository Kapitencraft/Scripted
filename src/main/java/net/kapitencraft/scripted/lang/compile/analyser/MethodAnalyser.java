package net.kapitencraft.scripted.lang.compile.analyser;

import net.kapitencraft.scripted.lang.env.abst.Leveled;
import net.kapitencraft.scripted.lang.func.LoxCallable;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;

import java.util.List;

public class MethodAnalyser extends Leveled<String, LoxCallable> {

    public boolean add(String name, LoxCallable callable) {
        if (this.getLast().containsKey(name)) return true;

        this.getLast().put(name, callable);
        return false;
    }

    public LoxClass type(String lexeme) {
        return getValue(lexeme).type();
    }

    public List<? extends LoxClass> args(String lexeme) {
        return getValue(lexeme).argTypes();
    }

    public LoxCallable callable(String lexeme) {
        return getValue(lexeme);
    }
}
