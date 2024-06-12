package net.kapitencraft.scripted.code.var;

import org.jetbrains.annotations.NotNull;

public class Var<T> {
    //there's no null value; add a new Var with the expected type but without value

    private T value;
    private final @NotNull VarType<T> type;

    public Var(@NotNull VarType<T> type) {
        this.type = type;
    }

    public Var(@NotNull VarType<T> type, T value) {
        this.value = value;
        this.type = type;
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
