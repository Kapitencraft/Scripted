package net.kapitencraft.scripted.lang.oop.clazz.primitive;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.algebra.OperationType;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;

public class DoubleClass extends PrimitiveClass {

    public DoubleClass() {
        super(VarTypeManager.NUMBER, "double", 0d);
    }

    @Override
    public ScriptedClass checkOperation(OperationType type, ClassReference other) {
        if (other.get().isChildOf(VarTypeManager.NUMBER)) {
            return type.isComparator() ? VarTypeManager.BOOLEAN : VarTypeManager.DOUBLE;
        }
        return VarTypeManager.VOID;
    }
}
