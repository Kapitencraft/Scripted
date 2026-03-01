package net.kapitencraft.scripted.lang.holder.class_ref;

import net.kapitencraft.scripted.lang.compiler.Holder;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class ClassReference implements Supplier<ScriptedClass> {
    protected ScriptedClass target;
    private final String name;
    private final String pck;

    public ClassReference(String name, String pck) {
        this.name = name;
        this.pck = pck;
    }

    public ScriptedClass get(@Nullable GenericStack generics) {
        return Objects.requireNonNull(target, "ScriptedClass not present: " + this.name);
    }

    public ScriptedClass get() {
        return get(null);
    }

    public String absoluteName() {
        return pck + "." + name;
    }

    public String pck() {
        return pck;
    }

    public static ClassReference of(ScriptedClass target) {
        ClassReference reference = new ClassReference(target.name(), target.pck());
        reference.setTarget(target);
        return reference;
    }

    public String name() {
        return name;
    }

    public void setTarget(ScriptedClass target) {
        this.target = target;
    }

    public boolean is(ScriptedClass scriptedClass) {
        return this.get().is(scriptedClass);
    }

    public boolean is(ClassReference reference) {
        return Objects.equals(reference.pck, this.pck) && Objects.equals(reference.name, this.name);
    }

    public ClassReference array() {
        return new ClassWrapper(this.name + "[]", this.pck, ScriptedClass::array, this);
    }

    @Override
    public String toString() {
        return "ClassReference@" + this.name + (exists() ? ", applied: " + target : "");
    }

    public boolean exists() {
        return target != null;
    }

    public Holder.Generics getGenerics() {
        return get().getGenerics();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || obj instanceof ClassReference cR && Objects.equals(cR.target, this.target);
    }
}
