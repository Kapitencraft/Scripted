package net.kapitencraft.scripted.tool;

import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.run.Interpreter;
import net.kapitencraft.scripted.lang.run.RuntimeError;

import static net.kapitencraft.scripted.lang.run.Interpreter.checkNumberOperands;

public class Math {

    public static Number mergeSub(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD - bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI - bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD - bI;
        } else {
            return (int)a - (int) b;
        }
    }

    public static Number mergeMul(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD * bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI * bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD * bI;
        } else {
            return (int)a * (int) b;
        }
    }

    public static Number mergeDiv(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD / bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI / bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD + bI;
        } else {
            return (int)a / (int) b;
        }
    }

    public static Number mergeMod(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD % bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI % bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD % bI;
        } else {
            return (int)a % (int) b;
        }
    }

    public static Number mergeAdd(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD + bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI + bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD + bI;
        } else {
            return (int)a + (int)b;
        }
    }

    public static boolean mergeLesser(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD < bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI < bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD < bI;
        } else {
            return (int)a < (int)b;
        }
    }

    public static boolean mergeGreater(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD > bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI > bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD > bI;
        } else {
            return (int)a > (int)b;
        }
    }

    public static boolean mergeLEqual(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD <= bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI <= bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD <= bI;
        } else {
            return (int)a < (int)b;
        }
    }

    public static boolean mergeGEqual(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD >= bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI >= bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD >= bI;
        } else {
            return (int)a >= (int)b;
        }
    }

    public static Object mergePow(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return java.lang.Math.pow(aD, bD);
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return java.lang.Math.pow(aI, bD);
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return java.lang.Math.pow(aD, bI);
        } else {
            return java.lang.Math.pow((int)a, (int)b);
        }
    }

    public static Object merge(Object activeVal, Object exprVal, Token type) {
        return switch (type.type()) {
            case AND_ASSIGN:
                yield Interpreter.isTruthy(activeVal) && Interpreter.isTruthy(exprVal);
            case OR_ASSIGN:
                yield Interpreter.isTruthy(activeVal) || Interpreter.isTruthy(exprVal);
            case XOR_ASSIGN:
                yield Interpreter.isTruthy(activeVal) ^ Interpreter.isTruthy(exprVal);
            case SUB_ASSIGN:
                checkNumberOperands(type, activeVal, exprVal);
                yield mergeSub(activeVal, exprVal);
            case DIV_ASSIGN:
                checkNumberOperands(type, activeVal, exprVal);
                yield mergeDiv(activeVal, exprVal);
            case MUL_ASSIGN:
                checkNumberOperands(type, activeVal, exprVal);
                yield mergeMul(activeVal, exprVal);
            case MOD_ASSIGN:
                checkNumberOperands(type, activeVal, exprVal);
                yield mergeMod(activeVal, exprVal);
            case ADD_ASSIGN:
                if (activeVal instanceof String lS) {
                    yield lS + exprVal;
                } else if (exprVal instanceof String vS) {
                    yield activeVal + vS;
                }

                try {
                    yield mergeAdd(activeVal, exprVal);
                } catch (Exception e) {
                    throw new RuntimeError(type, "Operands must be two numbers or two strings.");
                }

            default:
                throw new RuntimeError(type, "Unknown Operation type");
        };
    }
}
