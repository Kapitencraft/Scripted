package net.kapitencraft.scripted.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Objects;
import java.util.stream.Stream;

public interface JsonHelper {

    static <T> VarType<T> readType(JsonObject object, String name) {
        return (VarType<T>) ModRegistries.VAR_TYPES.getValue(new ResourceLocation(GsonHelper.getAsString(object, name)));
    }

    static <T> String saveType(VarType<T> type) {
        return Objects.requireNonNull(ModRegistries.VAR_TYPES.getKey(type), "unregistered VarType found: " + type).toString();
    }

    static Stream<JsonObject> castToObjects(JsonArray array) {
        return array.asList().stream().filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject);
    }
}
