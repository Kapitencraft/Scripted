package net.kapitencraft.scripted.lang.func;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.env.core.Environment;
import net.kapitencraft.scripted.lang.exception.CancelBlock;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;
import net.kapitencraft.scripted.lang.run.Interpreter;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.FuncDecl declaration;

    public LoxFunction(Stmt.FuncDecl declaration) {
        this.declaration = declaration;
    }

    @Override
    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
        if (declaration.body == null) {
            throw new IllegalAccessError("abstract method called directly! this shouldn't happen...");
        }

        for (int i = 0; i < declaration.params.size(); i++) {
            environment.defineVar(declaration.params.get(i).getSecond().lexeme(), arguments.get(i));
        }

        try {
            interpreter.interpret(declaration.body, environment);
        } catch (CancelBlock returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public boolean isAbstract() {
        return declaration.body == null;
    }

    @Override
    public LoxClass type() {
        return declaration.retType;
    }

    @Override
    public List<? extends LoxClass> argTypes() {
        return declaration.params.stream().map(Pair::getFirst).toList();
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<declared fn#" + declaration.name.lexeme() + ">";
    }
}