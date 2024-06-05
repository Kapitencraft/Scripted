package net.kapitencraft.scripted.code.oop;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.functions.abstracts.InstanceFunction;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;

public class FunctionMap<T> {
    private final HashMap<String, InstanceFunction<T>> map = new HashMap<>();


    public <J extends InstanceFunction<T>> void addFunction(String name, J function) {
        map.put(name, function);
    }

    public InstanceFunction<T>.Instance load(String type, JsonObject object, VarAnalyser analyser) {
        InstanceFunction<T> function = map.get(type);
        if (function == null) throw new IllegalArgumentException("Function type '" + GsonHelper.getAsString(object, "type") + "' does not exist");
        InstanceFunction<T>.Instance instance = function.load(object, analyser);
        instance.analyse(analyser); //instantly analyse to push new Vars to the analyser
        return instance;
    }
}
