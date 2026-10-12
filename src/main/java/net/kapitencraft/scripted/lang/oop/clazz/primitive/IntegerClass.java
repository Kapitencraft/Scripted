package net.kapitencraft.scripted.lang.oop.clazz.primitive;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.algebra.OperationType;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;

public class IntegerClass extends PrimitiveClass {
    public IntegerClass() {
        super(VarTypeManager.NUMBER, "int", 0);
    }

    @Override
    public ScriptedClass checkOperation(OperationType type, ClassReference other) {
        if (other.get().isChildOf(VarTypeManager.NUMBER)) {
            return type.isComparator() ? VarTypeManager.BOOLEAN : other.get();
        }
        return VarTypeManager.VOID;
    }
}
