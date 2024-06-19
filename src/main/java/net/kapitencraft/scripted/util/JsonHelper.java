package net.kapitencraft.scripted.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.edit.client.text.Compiler;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface JsonHelper {
    Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static <T> VarType<T> readType(JsonObject object, String name) {
        return (VarType<T>) ModRegistries.VAR_TYPES.getValue(new ResourceLocation(GsonHelper.getAsString(object, name)));
    }

    static <T> String saveType(VarType<T> type) {
        return Objects.requireNonNull(ModRegistries.VAR_TYPES.getKey(type), "unregistered VarType found: " + type).toString();
    }

    static Function.Instance readFunction(JsonObject object, VarAnalyser analyser) {
        String type = GsonHelper.getAsString(object, "type");
        if (type.contains(".")) { //reading instance functions
            String[] id = type.split("\\.");
            VarType<?> varType = ModRegistries.VAR_TYPES.getValue(new ResourceLocation(id[0]));
            if (varType == null) throw new JsonSyntaxException("unknown function key '" + id[0] + "'");
            return varType.buildFunction(id[1], object, analyser);
        }
        Function function = ModRegistries.FUNCTIONS.getValue(new ResourceLocation(type));
        if (function == null) throw new IllegalArgumentException("Function type '" + GsonHelper.getAsString(object, "type") + "' does not exist");
        Function.Instance instance = function.load(object, analyser);
        instance.analyse(analyser); //instantly analyse to push new Vars to the analyser
        return instance;
    }

    static JsonObject saveFunction(Function.Instance instance) {
        Function function = instance.getFunction();
        String name = Objects.requireNonNull(ModRegistries.FUNCTIONS.getKey(function), "unregistered Function found: " + function).toString();
        JsonObject object = new JsonObject();
        object.addProperty("type", name);
        instance.save(object);
        return object;
    }

    static <T, P> Method<T>.Instance readMethod(JsonObject object, VarAnalyser analyser, @Nullable Method<P>.Instance parent) {
        String type = GsonHelper.getAsString(object, "type");
        //instance methods have their own loading system;
        if (type.startsWith("new")) { //constructors
            VarType<P> varType = Compiler.readType(type.substring(3));
            return varType == null ? null : (Method<T>.Instance) varType.buildConstructor(object, analyser);
        } else { //"normal" methods
            Method<T> method = (Method<T>) ModRegistries.METHODS.getValue(new ResourceLocation(type));
            return method == null ? null : method.load(object, analyser);
        }
    }
}
