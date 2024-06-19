package net.kapitencraft.scripted.code.oop;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;

public class FieldMap<P> {
    private final HashMap<String, VarType<P>.Field<?>> fields = new HashMap<>();

    public FieldMap() {

    }

    public VarType<P>.Field<?> getOrThrow(String name) {
        VarType<P>.Field<?> field = fields.get(name);
        if (field == null) throw new IllegalArgumentException("Field '" + name + "' does not exist");
        return field;
    }

    @ApiStatus.Internal
    public void addField(String name, VarType<P>.Field<?> field) {
        if (fields.containsKey(name)) throw new IllegalArgumentException("tried to register field '" + name + "' twice");
        fields.put(name, field);
    }
}
