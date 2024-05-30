package net.kapitencraft.scripted.code.vars;

import java.util.HashMap;
import java.util.function.Supplier;

public class VarMap {
    private final HashMap<String, Var<?>> content = new HashMap<>();


    public <T> void addVar(String name, Supplier<VarType<T>> type) {
        content.put(name, new Var<>(type));
    }

    public <T> void setVar(String name, T value) {
        var(name).setValue(value);
    }

    private <T> Var<T> var(String name) {
        try {
            return (Var<T>) content.get(name);
        } catch (Exception e) {
            throw new IllegalStateException("found value of wrong type for variable '" + name + "'");
        }
    }

    public <T> T getVar(String name) {
        return (T) var(name).getValue();
    }
}
