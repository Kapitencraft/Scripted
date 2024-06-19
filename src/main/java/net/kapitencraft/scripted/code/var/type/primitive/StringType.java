package net.kapitencraft.scripted.code.var.type.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.minecraft.util.GsonHelper;

import java.util.regex.Pattern;

public class StringType extends PrimitiveType<String> {
    public static final Pattern STRING = Pattern.compile("^(\"(.+)\")$");
    public StringType() {
        super("String", (s, s1) -> s + s1, null, null, null, null, String::compareTo);
    }

    @Override
    public Pattern matcher() {
        return STRING;
    }

    @Override
    public String openRegex() {
        return "\"";
    }

    @Override
    public String closeRegex() {
        return "\"";
    }

    @Override
    public String loadPrimitive(String string) {
        return string;
    }

    @Override
    public void saveToJson(JsonObject object, String value) {
        object.addProperty("value", value);
    }

    @Override
    public String loadFromJson(JsonObject object) {
        return GsonHelper.getAsString(object, "value");
    }
}
