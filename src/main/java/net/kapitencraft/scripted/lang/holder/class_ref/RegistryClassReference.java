package net.kapitencraft.scripted.lang.holder.class_ref;

import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;

import java.util.function.Supplier;

public class RegistryClassReference extends ClassReference {
    private final Supplier<ScriptedClass> sup;

    public RegistryClassReference(String name, String pck, Supplier<ScriptedClass> sup) {
        super(name, pck);
        this.sup = sup;
    }

    public void create() {
        this.setTarget(sup.get());
    }
}
