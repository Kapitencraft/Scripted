package net.kapitencraft.scripted.code.var;

import java.util.function.Supplier;

public class Var<T> {
    private T value;

    public Var() {
    }

    public Var(T value) {
        this.value = value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public VarType<T> getType() {
        if (this.value == null) {
            throw new NullPointerException("can invoke method cuz value is null");
        }
        try {
            return (VarType<T>) VarManager.INSTANCE.getType(this.value.getClass());
        } catch (Exception e) {
            throw new NullPointerException("unable to get Type for value '" + this.value.getClass().getCanonicalName() + "': " + e.getMessage());
        }
    }
}
