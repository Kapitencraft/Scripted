package net.kapitencraft.scripted.code.oop;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.Objects;

public class MethodMap<T> {
    private final HashMap<String, InstanceMethod<T, ?>> builders = new HashMap<>();

    public InstanceMethod<?, ?>.Instance buildMethod(JsonObject object, VarAnalyser analyser, Method<T>.Instance parent) {
        String name = GsonHelper.getAsString(object, "name");
        ParamData set = ParamData.of(object, analyser);
        InstanceMethod<T, ?>.Instance method = builders.get(name).load(set, parent, object);
        if (object.has("then")) return method.loadChild(object.getAsJsonObject("then"), analyser);
        return method;
    }

    public void registerMethod(String name, InstanceMethod<T, ?> builder) {
        if (builders.containsKey(name)) throw new IllegalArgumentException("tried to register method '" + name + "' twice");
        builders.put(name, builder);
    }

    public InstanceMethod<T,?> getOrThrow(String name) {
        return Objects.requireNonNull(builders.get(name), "unknown method '" + name + "'");
    }
}
