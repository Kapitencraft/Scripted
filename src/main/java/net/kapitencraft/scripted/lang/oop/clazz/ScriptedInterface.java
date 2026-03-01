package net.kapitencraft.scripted.lang.oop.clazz;

import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.field.ScriptedField;
import net.kapitencraft.scripted.lang.run.VarTypeManager;

import java.util.Map;

public interface ScriptedInterface extends ScriptedClass {

    @Override
    default Map<String, ? extends ScriptedField> getFields() {
        return Map.of();
    }

    @Override
    default ClassReference getFieldType(String name) {
        return VarTypeManager.VOID.reference();
    }

    @Override
    default boolean hasField(String name) {
        return false;
    }

    @Override
    default short getModifiers() {
        return Modifiers.INTERFACE;
    }

    @Override
    default ClassReference superclass() {
        return VarTypeManager.VOID.reference();
    }
}
