package net.kapitencraft.scripted.lang.oop;

import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Package {
    private final Map<String, LoxClass> classes = new HashMap<>();
    private final Map<String, Package> packages = new HashMap<>();

    public boolean hasPackage(String name) {
        return packages.containsKey(name);
    }

    public boolean hasClass(String name) {
        return classes.containsKey(name);
    }

    public Package getPackage(String name) {
        return packages.get(name);
    }

    public LoxClass getClass(String name) {
        return classes.get(name);
    }

    public void addClass(String name, LoxClass cl) {
        classes.put(name, cl);
    }

    public void addPackage(String name, Package pck) {
        packages.put(name, pck);
    }

    public Package getOrCreatePackage(String name) {
        if (!hasPackage(name)) {
            addPackage(name, new Package());
        }
        return getPackage(name);
    }

    public Collection<LoxClass> allClasses() {
        return classes.values();
    }
}
