package net.kapitencraft.scripted.lang.oop.clazz.primitive;

import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.run.VarTypeManager;
import net.kapitencraft.scripted.lang.run.algebra.Operand;
import net.kapitencraft.scripted.lang.run.algebra.OperationType;

public class FloatClass extends PrimitiveClass {
    public FloatClass() {
        super(VarTypeManager.NUMBER, "float", 0f);
    }

    @Override
    public ScriptedClass checkOperation(OperationType type, Operand operand, ClassReference other) {
        if (other.get().isChildOf(VarTypeManager.NUMBER)) {
            return type.isComparator() ? VarTypeManager.BOOLEAN : other.get();
        }
        return VarTypeManager.VOID;
    }

    @Override
    public Object doOperation(OperationType type, Operand operand, Object self, Object other) {
        float val = (float) self;
        if (type == OperationType.ADDITION) {
            if (other instanceof Float f) {
                return val + f;
            }
            if (other instanceof Double d) {
                return val + d;
            }
            if (other instanceof Integer i) {
                return val + i;
            }
        }
        if (type == OperationType.SUBTRACTION) {
            if (other instanceof Float f) {
                return val - f;
            }
            if (other instanceof Double d) {
                return val - d;
            }
            if (other instanceof Integer i) {
                return val - i;
            }
        }
        if (type == OperationType.MULTIPLICATION) {
            if (other instanceof Float f) {
                return val * f;
            }
            if (other instanceof Double d) {
                return val * d;
            }
            if (other instanceof Integer i) {
                return val * i;
            }
        }
        if (type == OperationType.DIVISION) {
            if (other instanceof Float f) {
                return val / f;
            }
            if (other instanceof Double d) {
                return val / d;
            }
            if (other instanceof Integer i) {
                return val / i;
            }
        }
        if (type == OperationType.MODULUS) {
            if (other instanceof Float f) {
                return val % f;
            }
            if (other instanceof Double d) {
                return val % d;
            }
            if (other instanceof Integer i) {
                return val % i;
            }
        }
        if (type == OperationType.POTENCY) {
            return Math.pow(val, ((Number) other).doubleValue());
        }
        if (type == OperationType.LEQUAL) {
            if (other instanceof Float f) {
                return val <= f;
            }
            if (other instanceof Double d) {
                return val <= d;
            }
            if (other instanceof Integer i) {
                return val <= i;
            }
        }
        if (type == OperationType.NEQUAL) {
            if (other instanceof Float f) {
                return val != f;
            }
            if (other instanceof Double d) {
                return val != d;
            }
            if (other instanceof Integer i) {
                return val != i;
            }
        }
        if (type == OperationType.GEQUAL) {
            if (other instanceof Float f) {
                return val >= f;
            }
            if (other instanceof Double d) {
                return val >= d;
            }
            if (other instanceof Integer i) {
                return val >= i;
            }
        }
        if (type == OperationType.LESS) {
            if (other instanceof Float f) {
                return val < f;
            }
            if (other instanceof Double d) {
                return val < d;
            }
            if (other instanceof Integer i) {
                return val < i;
            }
        }
        if (type == OperationType.MORE) {
            if (other instanceof Float f) {
                return val > f;
            }
            if (other instanceof Double d) {
                return val > d;
            }
            if (other instanceof Integer i) {
                return val > i;
            }
        }
        if (type == OperationType.EQUAL) {
            if (other instanceof Float f) {
                return val == f;
            }
            if (other instanceof Double d) {
                return val == d;
            }
            if (other instanceof Integer i) {
                return val == i;
            }
        }
        return null;
    }
}
