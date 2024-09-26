package net.kapitencraft.scripted.lang.oop.clazz;

import net.kapitencraft.scripted.lang.env.core.Environment;
import net.kapitencraft.scripted.lang.func.LoxCallable;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.oop.ClassInstance;
import net.kapitencraft.scripted.lang.oop.NativeClassInstance;
import net.kapitencraft.scripted.lang.run.Interpreter;

import java.util.List;
import java.util.function.Function;

public abstract class NativeClass<T> implements LoxClass {
    private final Function<List<Object>, T> creator;

    protected NativeClass(Function<List<Object>, T> creator) {
        this.creator = creator;
    }

    @Override
    public ClassInstance createInst(List<Expr> params, Interpreter interpreter) {
        return new NativeClassInstance<>(this, interpreter, null);
    }

    @Override
    public LoxClass superclass() {
        return null;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return null;
    }

    @Override
    public LoxClass getStaticMethodType(String name) {
        return null;
    }

    @Override
    public LoxCallable getStaticMethod(String name) {
        return null;
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return false;
    }

    @Override
    public void callConstructor(Environment environment, Interpreter interpreter, List<Object> args) {

    }

    @Override
    public boolean isAbstract() {
        return false;
    }
}
