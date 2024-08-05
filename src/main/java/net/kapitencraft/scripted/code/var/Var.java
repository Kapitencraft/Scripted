package net.kapitencraft.scripted.code.var;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class Var<T> {

    private T value;
    private final @NotNull VarType<T> type;
    private final boolean isFinal;
    private boolean applied;

    public Var(@NotNull VarType<T> type, boolean isFinal) {
        this.type = type;
        this.isFinal = isFinal;
    }

    public Var(@NotNull VarType<T> type, T value, boolean isFinal) {
        this(type, isFinal);
        this.setValue(value);
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isApplied() {
        return applied;
    }

    public boolean notApplied() {
        return !isApplied();
    }

    protected void setApplied() {
        this.applied = true;
    }

    public void setValue(T value) {
        this.value = value;
        setApplied();
    }

    public T getValue() {
        if (!isApplied()) throw new IllegalStateException("Variable is not initialized");
        return value;
    }

    public @NotNull VarType<T> getType() {
        return this.type;
    }

    public boolean matchesType(Var<?> other) {
        return this.getType() == other.getType();
    }

    public boolean empty() {
        return !isApplied() || getValue() == null;
    }
}
