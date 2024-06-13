package net.kapitencraft.scripted.code.var;

import org.jetbrains.annotations.NotNull;

public class Var<T> {
    //there's no null value; add a new Var with the expected type but without value

    private T value;
    private final @NotNull VarType<T> type;
    private final boolean isFinal;

    public Var(@NotNull VarType<T> type, boolean isFinal) {
        this.type = type;
        this.isFinal = isFinal;
    }

    public Var(@NotNull VarType<T> type, T value, boolean isFinal) {
        this.value = value;
        this.type = type;
        this.isFinal = isFinal;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public @NotNull VarType<T> getType() {
        return this.type;
    }

    public boolean matchesType(Var<?> other) {
        return this.getType() == other.getType();
    }
}
