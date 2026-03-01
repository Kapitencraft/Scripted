package net.kapitencraft.scripted.lang.holder.token;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

public record RuntimeToken(String lexeme, int line) {
    public static RuntimeToken createNative(String value) {
        return new RuntimeToken(value, -1);
    }

    public static RuntimeToken fromJson(JsonObject object) {
        String lexeme = GsonHelper.getAsString(object, "lexeme");
        int line = GsonHelper.getAsInt(object, "line");
        return new RuntimeToken(lexeme, line);
    }

    public static RuntimeToken readFromSubObject(JsonObject object, String name) {
        return fromJson(GsonHelper.getAsJsonObject(object, name));
    }
}
