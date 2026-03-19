package net.kapitencraft.scripted.lang.oop.field;

import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;

public record SkeletonField(ClassReference type, short modifiers) implements ScriptedField {

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(modifiers);
    }
}
