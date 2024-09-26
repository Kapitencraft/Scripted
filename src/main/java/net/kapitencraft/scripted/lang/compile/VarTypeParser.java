package net.kapitencraft.scripted.lang.compile;

import net.kapitencraft.scripted.lang.VarTypeManager;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VarTypeParser {
    private final Map<String, LoxClass> implemented = new HashMap<>();

    public VarTypeParser() {
        implemented.putAll(VarTypeManager.getPackage("scripted.lang").allClasses().stream().collect(Collectors.toMap(LoxClass::name, Function.identity())));
    }

    public boolean hasClass(String clazz) {
        return implemented.containsKey(clazz);
    }

    public LoxClass getClass(String clazz) {
        return implemented.get(clazz);
    }

    public void addClass(LoxClass clazz) {
        implemented.put(clazz.name(), clazz);
    }
}
