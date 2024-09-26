package net.kapitencraft.scripted.lang.holder.ast;

import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;

import java.util.List;
import java.util.Map;

public abstract class Expr {

    public interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitSpecialAssignExpr(SpecialAssign expr);
        R visitBinaryExpr(Binary expr);
        R visitWhenExpr(When expr);
        R visitCallExpr(Call expr);
        R visitInstCallExpr(InstCall expr);
        R visitGetExpr(Get expr);
        R visitSetExpr(Set expr);
        R visitSpecialSetExpr(SpecialSet expr);
        R visitSwitchExpr(Switch expr);
        R visitCastCheckExpr(CastCheck expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitUnaryExpr(Unary expr);
        R visitVarRefExpr(VarRef expr);
        R visitFuncRefExpr(FuncRef expr);
        R visitConstructorExpr(Constructor expr);
    }

    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;
        public final Token type;

        public Assign(Token name, Expr value, Token type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class SpecialAssign extends Expr {
        public final Token name;
        public final Token assignType;

        public SpecialAssign(Token name, Token assignType) {
            this.name = name;
            this.assignType = assignType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialAssignExpr(this);
        }
    }

    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class When extends Expr {
        public final Expr condition;
        public final Expr ifTrue;
        public final Expr ifFalse;

        public When(Expr condition, Expr ifTrue, Expr ifFalse) {
            this.condition = condition;
            this.ifTrue = ifTrue;
            this.ifFalse = ifFalse;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhenExpr(this);
        }
    }

    public static class Call extends Expr {
        public final Expr callee;
        public final Token bracket;
        public final List<Expr> args;

        public Call(Expr callee, Token bracket, List<Expr> args) {
            this.callee = callee;
            this.bracket = bracket;
            this.args = args;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    public static class InstCall extends Expr {
        public final Expr callee;
        public final Token name;
        public final Token bracket;
        public final List<Expr> args;

        public InstCall(Expr callee, Token name, Token bracket, List<Expr> args) {
            this.callee = callee;
            this.name = name;
            this.bracket = bracket;
            this.args = args;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitInstCallExpr(this);
        }
    }

    public static class Get extends Expr {
        public final Expr object;
        public final Token name;

        public Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    public static class Set extends Expr {
        public final Expr object;
        public final Token name;
        public final Expr value;
        public final Token assignType;

        public Set(Expr object, Token name, Expr value, Token assignType) {
            this.object = object;
            this.name = name;
            this.value = value;
            this.assignType = assignType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    public static class SpecialSet extends Expr {
        public final Expr callee;
        public final Token name;
        public final Token assignType;

        public SpecialSet(Expr callee, Token name, Token assignType) {
            this.callee = callee;
            this.name = name;
            this.assignType = assignType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialSetExpr(this);
        }
    }

    public static class Switch extends Expr {
        public final Expr provider;
        public final Map<Object,Expr> params;
        public final Expr defaulted;
        public final Token keyword;

        public Switch(Expr provider, Map<Object,Expr> params, Expr defaulted, Token keyword) {
            this.provider = provider;
            this.params = params;
            this.defaulted = defaulted;
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchExpr(this);
        }
    }

    public static class CastCheck extends Expr {
        public final Expr object;
        public final LoxClass targetType;
        public final Token patternVarName;

        public CastCheck(Expr object, LoxClass targetType, Token patternVarName) {
            this.object = object;
            this.targetType = targetType;
            this.patternVarName = patternVarName;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCastCheckExpr(this);
        }
    }

    public static class Grouping extends Expr {
        public final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Literal extends Expr {
        public final Token value;

        public Literal(Token value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Logical extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    public static class Unary extends Expr {
        public final Token operator;
        public final Expr right;

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class VarRef extends Expr {
        public final Token name;

        public VarRef(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarRefExpr(this);
        }
    }

    public static class FuncRef extends Expr {
        public final Token name;

        public FuncRef(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFuncRefExpr(this);
        }
    }

    public static class Constructor extends Expr {
        public final LoxClass target;
        public final List<Expr> params;

        public Constructor(LoxClass target, List<Expr> params) {
            this.target = target;
            this.params = params;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitConstructorExpr(this);
        }
    }

  public abstract <R> R accept(Visitor<R> visitor);
}
