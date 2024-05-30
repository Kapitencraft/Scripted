package net.kapitencraft.scripted.code.vars;

import net.kapitencraft.scripted.init.VarTypes;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Var<T> {
    public static final Var<?> EMPTY = new Var<>(()->null);
    private final Supplier<VarType<T>> varType;
    private @Nullable T value;

    public Var(Supplier<VarType<T>> varType) {
        this.varType = varType;
    }

    public @Nullable T getValue() {
        return value;
    }

    public void setValue(@Nullable T value) {
        this.value = value;
    }

    public boolean isType(Supplier<? extends VarType<?>> other) {
        return other.get() == VarTypes.WILDCARD.get() || this.varType.get() == other.get();
    }
}