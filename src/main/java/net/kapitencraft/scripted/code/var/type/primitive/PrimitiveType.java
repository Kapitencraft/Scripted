package net.kapitencraft.scripted.code.var.type.primitive;

import net.kapitencraft.scripted.code.var.VarType;

import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

public class PrimitiveType<T> extends VarType<T> {
    public PrimitiveType(BiFunction<T, T, T> add, BiFunction<T, T, T> mult, BiFunction<T, T, T> div, BiFunction<T, T, T> sub, BiFunction<T, T, T> mod, ToDoubleFunction<T> comp) {
        super(add, mult, div, sub, mod, comp);
    }
}
