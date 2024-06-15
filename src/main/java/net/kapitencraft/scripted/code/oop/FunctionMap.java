package net.kapitencraft.scripted.code.oop;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.Objects;

public class FunctionMap<T> {
    private final HashMap<String, VarType<T>.InstanceFunction> map = new HashMap<>();


    public <J extends VarType<T>.InstanceFunction> void addFunction(String name, J function) {
        map.put(name, function);
    }

    public VarType<T>.InstanceFunction.Instance load(String type, JsonObject object, VarAnalyser analyser) {
        VarType<T>.InstanceFunction function = map.get(type);
        if (function == null) throw new IllegalArgumentException("Function type '" + GsonHelper.getAsString(object, "type") + "' does not exist");
        VarType<T>.InstanceFunction.Instance instance = function.load(object, analyser);
        instance.analyse(analyser); //instantly analyse to push new Vars to the analyser
        return instance;
    }

    public VarType<T>.InstanceFunction getOrTrow(String name) {
        return Objects.requireNonNull(map.get(name), "unknown function name '" + name + "'");
    }
}
