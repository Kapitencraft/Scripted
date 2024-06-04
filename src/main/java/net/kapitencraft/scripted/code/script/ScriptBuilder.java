package net.kapitencraft.scripted.code.script;

import java.util.HashMap;
import java.util.function.Function;

public class ScriptBuilder<T> {

    private final HashMap<String, Function<T, ?>> mappers;

    public <K> K getValue(String name, T in) {
        return (K) mappers.get(name).apply(in);
    }
}
