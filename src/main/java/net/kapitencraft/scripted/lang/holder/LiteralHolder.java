package net.kapitencraft.scripted.lang.holder;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.run.VarTypeManager;
import net.minecraft.util.GsonHelper;

public record LiteralHolder(Object value, ScriptedClass type) {

    public static final LiteralHolder EMPTY = new LiteralHolder(null, null);

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        if (this == EMPTY) return object;
        if (type == VarTypeManager.INTEGER) {
            object.addProperty("type", "int");
            object.addProperty("value", (Integer)value);
        } else if (type == VarTypeManager.FLOAT) {
            object.addProperty("type", "float");
            object.addProperty("value", (Float)value);
        } else if (type == VarTypeManager.DOUBLE) {
            object.addProperty("type", "double");
            object.addProperty("value", (Double)value);
        } else if (type == VarTypeManager.BOOLEAN) {
            object.addProperty("type", "bool");
            object.addProperty("value", (Boolean)value);
        } else if (type == VarTypeManager.CHAR) {
            object.addProperty("type", "char");
            object.addProperty("value", (Character)value);
        } else if (type.is(VarTypeManager.STRING.get())) {
            object.addProperty("type", "String");
            object.addProperty("value", (String)value);
        }
        return object;
    }

    public static LiteralHolder fromJson(JsonObject object) {
        if (object.isEmpty()) return EMPTY;
        String type = GsonHelper.getAsString(object, "type");
        ScriptedClass target = switch (type) {
            case "int" -> VarTypeManager.INTEGER;
            case "float" -> VarTypeManager.FLOAT;
            case "double" -> VarTypeManager.DOUBLE;
            case "bool" -> VarTypeManager.BOOLEAN;
            case "char" -> VarTypeManager.CHAR;
            case "String" -> VarTypeManager.STRING.get();
            default -> throw new IllegalArgumentException("unknown primitive type");
        };
        Object val = null;
        if (object.has("value")) {
            if (target == VarTypeManager.INTEGER) {
                val = GsonHelper.getAsInt(object, "value");
            } else if (target == VarTypeManager.FLOAT) {
                val = GsonHelper.getAsFloat(object, "value");
            } else if (target == VarTypeManager.DOUBLE) {
                val = GsonHelper.getAsDouble(object, "value");
            } else if (target == VarTypeManager.BOOLEAN) {
                val = GsonHelper.getAsBoolean(object, "value");
            } else if (target == VarTypeManager.CHAR) {
                val = GsonHelper.getAsCharacter(object, "value");
            } else
                val = GsonHelper.getAsString(object, "value");
        }
        return new LiteralHolder(val, target);
    }
}
