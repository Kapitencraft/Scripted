package net.kapitencraft.scripted.code.method.param;

import net.kapitencraft.scripted.code.vars.Var;
import net.kapitencraft.scripted.code.vars.VarMap;
import net.kapitencraft.scripted.code.vars.VarType;

import java.util.HashMap;
import java.util.function.Supplier;

public class ParamBuilder {
    private final HashMap<String, Supplier<? extends VarType<?>>> types = new HashMap<>();

    public ParamBuilder addParam(String name, Supplier<? extends VarType<?>> type) {
        if (types.containsKey(name)) throw new IllegalArgumentException("attempted to create param which was already existing (" + name + ")");
        types.put(name, type);
        return this;
    }

    public boolean hasParams(VarMap map) {
        boolean[] ar = new boolean[] { true };
        types.forEach((s, type) -> {
            Var<?> var = map.getVar(s);
            if (!var.isType(type)) ar[0] = false;
        });
        return ar[0];
    }
}
