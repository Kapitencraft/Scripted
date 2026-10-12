package net.kapitencraft.scripted.lang.holder.ast;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;

public abstract class SwitchKey {
    private final Token source;
    public final Expr expr;
    public int index;

    protected SwitchKey(Token source, Expr expr) {
        this.source = source;
        this.expr = expr;
    }

    public abstract boolean canMatch(ClassReference type);

    public Token getSource() {
        return source;
    }

    public abstract ClassReference getType();

    public static class Number extends SwitchKey {
        public Number(Token source, Expr expr) {
            super(source, expr);
        }

        @Override
        public boolean canMatch(ClassReference type) {
            return type.get().isChildOf(VarTypeManager.NUMBER);
        }

        @Override
        public ClassReference getType() {
            return this.getSource().literal().type().reference();
        }
    }

    public static class String extends SwitchKey {
        public String(Token source, Expr expr) {
            super(source, expr);
        }

        @Override
        public boolean canMatch(ClassReference type) {
            return type.is(VarTypeManager.STRING);
        }

        @Override
        public ClassReference getType() {
            return VarTypeManager.STRING;
        }
    }

    public static class Identifier extends SwitchKey {
        public Identifier(Token source, Expr expr) {
            super(source, expr);
        }

        @Override
        public boolean canMatch(ClassReference type) {
            return type.isChildOf(VarTypeManager.ENUM);
        }

        @Override
        public ClassReference getType() {
            return VarTypeManager.ENUM;
        }
    }

    public static class Illegal extends SwitchKey {

        public Illegal(Token source, Expr expr) {
            super(source, expr);
        }

        @Override
        public boolean canMatch(ClassReference type) {
            return false;
        }

        @Override
        public ClassReference getType() {
            return null;
        }
    }
}
