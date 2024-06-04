package net.kapitencraft.scripted.code.oop;

import net.kapitencraft.scripted.code.var.VarType;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Field<P, T>{
    private final Function<P, T> getter;
    private final BiConsumer<P, T> setter;
    private final Supplier<VarType<T>> type;

    public Field(Function<P, T> getter, BiConsumer<P, T> setter, Supplier<VarType<T>> type) {
        this.getter = getter;
        this.setter = setter;
        this.type = type;
    }


    public VarType<T> getType() {
        return type.get();
    }

    public T getValue(P in) {
        if (in == null) throw new NullPointerException("can not read field '" + this + "'");
        return getter.apply(in);
    }

    public void setValue(P in, T value) {
        setter.accept(in, value);
    }
}
