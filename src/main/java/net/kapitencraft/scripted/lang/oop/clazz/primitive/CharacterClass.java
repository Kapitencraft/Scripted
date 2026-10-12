package net.kapitencraft.scripted.lang.oop.clazz.primitive;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.algebra.OperationType;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;

public class CharacterClass extends PrimitiveClass {
    public CharacterClass() {
        super("char", ' ');
    }

    @Override
    public ScriptedClass checkOperation(OperationType type, ClassReference other) {
        if (other.is(VarTypeManager.INTEGER)) {
            return type.isComparator() ? VarTypeManager.BOOLEAN : VarTypeManager.INTEGER;
        }
        return VarTypeManager.VOID;
    }
}
