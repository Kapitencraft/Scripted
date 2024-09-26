package net.kapitencraft.scripted.code.var.type.primitive;

import com.google.gson.JsonPrimitive;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import org.jetbrains.annotations.NotNull;

public class BooleanType extends PrimitiveType<Boolean> {
    public BooleanType() {
        super("bool",  null, null, null, null, null, Boolean::compareTo);
    }

    @Override
    public Class<Boolean> getTypeClass() {
        return boolean.class; //why is there an extra class
    }

    @Override
    public @NotNull String toId() {
        return "Z"; //don't ask me why java uses Z for booleans... (maybe because of bytes?)
    }

    @Override
    public Boolean loadPrimitive(String string) {
        return Boolean.valueOf(string);
    }

    @Override
    public JsonPrimitive saveToJson(Boolean value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Boolean loadFromJson(JsonPrimitive prim) {
        return prim.getAsBoolean();
    }
}
