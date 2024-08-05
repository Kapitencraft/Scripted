package net.kapitencraft.scripted.code.exe.param;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.HashMap;

public class WildCardData {

    private final HashMap<String, VarType<?>> typesForCards = new HashMap<>();

    public WildCardData() {
    }

    public static WildCardData empty() {
        return new WildCardData();
    }

    public <T> VarType<T> getType(String name) {
        return (VarType<T>) typesForCards.get(name);
    }

    public <T> void applyType(String name, VarType<T> type) {
        typesForCards.put(name, type);
    }
}
