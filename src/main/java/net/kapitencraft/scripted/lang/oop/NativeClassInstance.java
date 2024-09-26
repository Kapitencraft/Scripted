package net.kapitencraft.scripted.lang.oop;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.lang.run.Interpreter;

public class NativeClassInstance<T> extends ClassInstance {
    private final T value;

    public VarType<T> getType() {
        return (VarType<T>) super.getType();
    }

    public NativeClassInstance(VarType<?> type, Interpreter interpreter, T value) {
        super(type, interpreter);
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
