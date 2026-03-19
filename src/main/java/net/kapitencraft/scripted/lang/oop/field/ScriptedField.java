package net.kapitencraft.scripted.lang.oop.field;

import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;

public interface ScriptedField {

    ClassReference type();

    short modifiers();

    boolean isFinal();

    boolean isStatic();
}