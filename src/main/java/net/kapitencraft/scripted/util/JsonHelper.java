package net.kapitencraft.scripted.util;

import com.google.gson.*;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Objects;
import java.util.stream.Stream;

public interface JsonHelper {
    Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static <T> VarType<T> readType(JsonObject object, String name) {
        return (VarType<T>) ModRegistries.VAR_TYPES.getValue(new ResourceLocation(GsonHelper.getAsString(object, name)));
    }

    static <T> String saveType(VarType<T> type) {
        return Objects.requireNonNull(ModRegistries.VAR_TYPES.getKey(type), "unregistered VarType found: " + type).toString();
    }

    static <T> Method<T>.Instance readMethodChain(JsonObject object, VarAnalyser analyser) {
        String type = GsonHelper.getAsString(object, "type");
        Method<?>.Instance inst;
        if (type.startsWith("new")) {
            VarType<?> varType = VarType.NAME_MAP.get(type.substring(3));
            inst = varType.buildConstructor(object, analyser);
        } else {
            Method<?> method = ModRegistries.METHODS.getValue(new ResourceLocation(type)); //1.21 update; i'm a cry
            if (method == null) throw new IllegalStateException("couldn't find method called '" + type + "'");
            inst = (Method<?>.Instance) method.load(object, analyser);
        }
        if (object.has("then")) {
            return (Method<T>.Instance) inst.loadChild(GsonHelper.getAsJsonObject(object, "then"), analyser);
        }
        return (Method<T>.Instance) inst;
    }

    static Stream<JsonObject> castToObjects(JsonArray array) {
        return array.asList().stream().filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject);
    }
}
