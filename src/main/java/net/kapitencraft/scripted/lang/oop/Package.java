package net.kapitencraft.scripted.lang.oop;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Package {
    private final String name;
    private final Map<String, ClassReference> classes = new HashMap<>();
    private final Map<String, Package> packages = new HashMap<>();

    public Package(String name) {
        this.name = name;
    }

    public boolean hasPackage(String name) {
        return packages.containsKey(name);
    }

    public boolean hasClass(String name) {
        return classes.containsKey(name);
    }

    public Package getPackage(String name) {
        return packages.get(name);
    }

    public ClassReference getClass(String name) {
        return classes.get(name);
    }

    public void addClass(String name, ScriptedClass cl) {
        if (!classes.containsKey(name))
            classes.put(name, ClassReference.of(cl));
        else
            classes.get(name).setTarget(cl);
    }

    public void addNullableClass(String name, @Nullable ScriptedClass cl) {
        if (cl == null) return;
        VarTypeManager.registerFlat(cl);
        addClass(name, cl);
    }

    public void addClass(String name, ClassReference reference) {
        if (name.contains("$")) {
            String[] split = name.split("\\$");
            this.classes.put(split[split.length - 1], reference);
        } else
            this.classes.put(name, reference);
    }

    //use getOrCreatePackage instead
    private void addPackage(String name, Package pck) {
        packages.put(name, pck);
    }

    public synchronized Package getOrCreatePackage(String name) {
        if (!hasPackage(name)) {
            addPackage(name, new Package(this.name.isEmpty() ? name : this.name + "." + name));
        }
        return getPackage(name);
    }

    public Collection<ClassReference> allClasses() {
        return classes.values();
    }

    public Collection<Package> allPackages() {
        return packages.values();
    }

    public String getName() {
        return name;
    }

    /**
     * may only be called from compilation
     */
    @ApiStatus.Internal
    public synchronized ClassReference getOrCreateClass(String name) {
        if (classes.containsKey(name)) return classes.get(name);
        ClassReference reference = new ClassReference(name, this.name);
        classes.put(name, reference);
        return reference;
    }

    @Override
    public String toString() {
        return "Pck@" + this.name;
    }
}
