package net.kapitencraft.scripted.lang.holder.class_ref;

import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.scripted.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrimitiveClassReference extends ClassReference {

    public PrimitiveClassReference(String name, @NotNull PrimitiveClass target) {
        super(name, "scripted.lang");
        this.target = target;
    }

    @Override
    public void setTarget(ScriptedClass target) {
        throw new IllegalAccessError("can not modify target of primitive type");
    }

    @Override
    public ScriptedClass get(@Nullable GenericStack generics) {
        return super.get(generics);
    }

    @Override
    public boolean exists() {
        return true;
    }
}
