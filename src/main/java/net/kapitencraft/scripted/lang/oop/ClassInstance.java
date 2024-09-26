package net.kapitencraft.scripted.lang.oop;

import net.kapitencraft.scripted.lang.env.core.Environment;
import net.kapitencraft.scripted.lang.func.LoxCallable;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;
import net.kapitencraft.scripted.lang.run.Interpreter;
import net.kapitencraft.scripted.tool.Math;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassInstance {
    private final Environment environment;
    private final Map<String, Object> fields = new HashMap<>();
    private final LoxClass type;

    public LoxClass getType() {
        return type;
    }

    public ClassInstance(LoxClass type, Interpreter interpreter) {
        this.environment = new Environment();
        environment.defineVar("this", this);
        this.type = type;
        this.executeConstructor(interpreter);
    }

    private void executeConstructor(Interpreter interpreter) {
        this.type.getFields().forEach((string, loxField) -> this.fields.put(string, loxField.initialize(this.environment, interpreter)));
    }

    public Object assignField(String name, Object val) {
        this.fields.put(name, val);
        return getField(name);
    }

    public Object assignFieldWithOperator(String name, Object val, Token type) {
        return this.assignField(name, Math.merge(getField(name), val, type));
    }

    public Object specialAssign(String name, Token assignType) {
        Object val = this.fields.get(name);
        if (val instanceof Integer) {
            this.assignField(name, (int)val + (assignType.type() == TokenType.GROW ? 1 : -1));
        } else {
            this.assignField(name, (double)val + (assignType.type() == TokenType.GROW ? 1 : -1));
        }
        return getField(name);
    }


    public Object getField(String name) {
        return this.fields.get(name);
    }

    public void construct(List<Expr> params, Interpreter interpreter) {
        type.callConstructor(this.environment, interpreter, interpreter.visitArgs(params));
    }

    public Object executeMethod(String name, List<Object> arguments, Interpreter interpreter) {
        LoxCallable callable = type.getMethod(name);
        this.environment.push();
        Object val = callable.call(this.environment, interpreter, arguments);
        this.environment.pop();
        return val;
    }

}
