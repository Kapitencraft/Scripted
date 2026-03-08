package net.kapitencraft.scripted.lang.holder.ast;

import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;

import java.util.Map;

public interface Expr {

    interface Visitor<R> {
        R visitVarRefExpr(VarRef expr);

        R visitSetExpr(Set expr);

        R visitArraySpecialExpr(ArraySpecial expr);

        R visitRegistryAccessExpr(RegistryAccess expr);

        R visitInstCallExpr(InstCall expr);

        R visitLogicalExpr(Logical expr);

        R visitSuperCallExpr(SuperCall expr);

        R visitCastCheckExpr(CastCheck expr);

        R visitArrayGetExpr(ArrayGet expr);

        R visitLiteralExpr(Literal expr);

        R visitArrayConstructorExpr(ArrayConstructor expr);

        R visitStaticSpecialExpr(StaticSpecial expr);

        R visitSpecialSetExpr(SpecialSet expr);

        R visitArraySetExpr(ArraySet expr);

        R visitSpecialAssignExpr(SpecialAssign expr);

        R visitConstructorExpr(Constructor expr);

        R visitStaticSetExpr(StaticSet expr);

        R visitGroupingExpr(Grouping expr);

        R visitUnaryExpr(Unary expr);

        R visitWhenExpr(When expr);

        R visitStaticGetExpr(StaticGet expr);

        R visitSwitchExpr(Switch expr);

        R visitSliceExpr(Slice expr);

        R visitGetExpr(Get expr);

        R visitAssignExpr(Assign expr);

        R visitStaticCallExpr(StaticCall expr);

        R visitBinaryExpr(Binary expr);
    }

    <R> R accept(Visitor<R> visitor);

    record VarRef(
            Token name,
            byte ordinal
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarRefExpr(this);
        }
    }

    record Set(
            Expr object,
            Token name,
            Expr value,
            Token assignType,
            ClassReference executor
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    record ArraySpecial(
            Expr object,
            Expr index,
            Token assignType,
            ClassReference executor
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySpecialExpr(this);
        }
    }

    record RegistryAccess(
            ClassReference type,
            Token origin,
            String regKey,
            String valKey
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitRegistryAccessExpr(this);
        }
    }

    record InstCall(
            Expr callee,
            Token name,
            Expr[] args,
            ClassReference retType,
            String id
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitInstCallExpr(this);
        }
    }

    record Logical(
            Expr left,
            Token operator,
            Expr right
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    record SuperCall(
            Expr callee,
            ClassReference type,
            Token name,
            Expr[] args,
            ClassReference retType,
            String id
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperCallExpr(this);
        }
    }

    record CastCheck(
            Expr object,
            ClassReference targetType,
            Token patternVarName
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCastCheckExpr(this);
        }
    }

    record ArrayGet(
            Expr object,
            Expr index,
            ClassReference type
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayGetExpr(this);
        }
    }

    record Literal(
            Token literal
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    record ArrayConstructor(
            Token keyword,
            ClassReference compoundType,
            Expr size,
            Expr[] obj
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayConstructorExpr(this);
        }
    }

    record StaticSpecial(
            ClassReference target,
            Token name,
            Token assignType,
            ClassReference executor
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSpecialExpr(this);
        }
    }

    record SpecialSet(
            Expr callee,
            Token name,
            Token assignType,
            ClassReference retType
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialSetExpr(this);
        }
    }

    record ArraySet(
            Expr object,
            Expr index,
            Expr value,
            Token assignType,
            ClassReference executor
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySetExpr(this);
        }
    }

    record SpecialAssign(
            Token name,
            Token assignType,
            int ordinal,
            ClassReference executor
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialAssignExpr(this);
        }
    }

    record Constructor(
            Token keyword,
            ClassReference target,
            Expr[] args,
            String signature
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitConstructorExpr(this);
        }
    }

    record StaticSet(
            ClassReference target,
            Token name,
            Expr value,
            Token assignType,
            ClassReference executor
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSetExpr(this);
        }
    }

    record Grouping(
            Expr expression
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    record Unary(
            Token operator,
            Expr right,
            ClassReference executor
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    record When(
            Expr condition,
            Expr ifTrue,
            Expr ifFalse
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhenExpr(this);
        }
    }

    record StaticGet(
            ClassReference target,
            Token name
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticGetExpr(this);
        }
    }

    record Switch(
            Expr provider,
            Map<Integer, Expr> params,
            Expr defaulted,
            Token keyword
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchExpr(this);
        }
    }

    record Slice(
            Expr object,
            Expr start,
            Expr end,
            Expr interval
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSliceExpr(this);
        }
    }

    record Get(
            Expr object,
            Token name,
            ClassReference type
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    record Assign(
            Token name,
            Expr value,
            Token type,
            byte ordinal,
            ClassReference executor
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    record StaticCall(
            ClassReference target,
            Token name,
            Expr[] args,
            ClassReference retType,
            String id
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticCallExpr(this);
        }
    }

    record Binary(
            Expr left,
            Expr right,
            Token operator,
            ClassReference executor,
            ClassReference retType
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }
}
