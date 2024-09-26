package net.kapitencraft.scripted.lang.env.core;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.lang.env.abst.Leveled;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.oop.NativeClassInstance;
import net.kapitencraft.scripted.tool.Math;

import static net.kapitencraft.scripted.lang.run.Interpreter.checkNumberOperand;
import static net.kapitencraft.scripted.lang.run.Interpreter.in;

public class VarEnv extends Leveled<String, VarEnv.Wrapper> {

    VarEnv() {
    }

    public void define(String name, Object value) {
        this.addValue(name, new Wrapper(value));
    }

    public Object get(String name) {
        return getValue(name).val;
    }

    public void assign(String name, Object value) {
        getValue(name).val = value;
    }

    public Object assignWithOperator(Token type, String name, Object value) {
        Object current = get(name);
        this.assign(name, Math.merge(current, value, type));
        return get(name);
    }

    public Object specialAssign(String name, Token type) {
        Object value = get(name);
        checkNumberOperand(type, value);
        if (value instanceof Integer) {
            this.assign(name, (int) value + (type.type() == TokenType.GROW ? 1 : -1));
        } else if (value instanceof Double)
            this.assign(name, (double) value + (type.type() == TokenType.GROW ? 1 : -1));
        else if (value instanceof NativeClassInstance<?> instance) {
            this.assign(name, nativeSpecialAssign(instance, type.type() == TokenType.GROW));
        }
        return get(name);
    }

    private <T> T nativeSpecialAssign(NativeClassInstance<T> val, boolean grow) {
        VarType<T> type = val.getType();
        return type.add(val.getValue(), grow ? type.one() : type.negOne());
    }

    static class Wrapper {
        Object val;

        public Wrapper(Object in) {
            val = in;
        }
    }
}