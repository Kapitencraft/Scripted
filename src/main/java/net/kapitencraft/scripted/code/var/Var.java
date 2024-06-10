package net.kapitencraft.scripted.code.var;

import net.kapitencraft.scripted.init.ModVarTypes;
import org.jetbrains.annotations.NotNull;

public class Var<T> {
    public static final Var<?> NULL = new Var<>(ModVarTypes.VAR_TYPE.get());

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
        if (this == NULL || other == NULL) {
            return true;
        }
        return this.getType() == other.getType();
    }
}
