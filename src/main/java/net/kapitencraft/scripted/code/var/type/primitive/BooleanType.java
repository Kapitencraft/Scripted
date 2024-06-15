package net.kapitencraft.scripted.code.var.type.primitive;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.regex.Pattern;

public class BooleanType extends PrimitiveType<Boolean> {
    private static final Pattern PATTERN = Pattern.compile("^((true)|(false))$");
    public BooleanType() {
        super(null, null, null, null, null, null);
    }

    @Override
    public Pattern matcher() {
        return PATTERN;
    }

    @Override
    public Boolean loadPrimitive(String string) {
        return Boolean.valueOf(string);
    }

    @Override
    public void saveToJson(JsonObject object, Boolean value) {
        object.addProperty("value", value);
    }

    @Override
    public Boolean loadFromJson(JsonObject object) {
        return GsonHelper.getAsBoolean(object, "value");
    }
}
