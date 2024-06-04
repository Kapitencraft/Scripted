package net.kapitencraft.scripted.code.oop;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;

public class FieldMap<P> {
    private final HashMap<String, Field<P, ?>> fields = new HashMap<>();

    public FieldMap() {

    }

    public Field<P, ?> getOrThrow(String name) {
        Field<P, ?> field = fields.get(name);
        if (field == null) throw new IllegalArgumentException("Field '" + name + "' does not exist");
        return field;
    }

    @ApiStatus.Internal
    public void addField(String name, Field<P, ?> field) {
        if (fields.containsKey(name)) throw new IllegalArgumentException("tried to register field '" + name + "' twice");
        fields.put(name, field);
    }
}
