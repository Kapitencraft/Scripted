package net.kapitencraft.scripted.lang.oop;

import net.kapitencraft.scripted.lang.env.core.Environment;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;
import net.kapitencraft.scripted.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.scripted.lang.run.Interpreter;

public class GeneratedField extends LoxField {
    private final Stmt.VarDecl decl;

    public GeneratedField(Stmt.VarDecl decl) {
        this.decl = decl;
    }

    @Override
    public Object initialize(Environment environment, Interpreter interpreter) {
        if (decl.initializer == null) {
            if (decl.type instanceof PrimitiveClass prim) {
                return prim.defaultValue();
            }
            return null;
        }
        return interpreter.evaluate(decl.initializer);
    }

    @Override
    public LoxClass getType() {
        return decl.type;
    }
}
