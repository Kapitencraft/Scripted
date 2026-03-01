package net.kapitencraft.scripted.lang.compiler.analyser;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import org.jetbrains.annotations.Contract;

public class RetTypeAnalyser implements Expr.Visitor<ClassReference> {
    private final BytecodeVars varAnalyser;

    //STAGE: Skeleton

    @Contract(pure = true)
    public ClassReference findRetType(Expr expr) {
        return expr.accept(this);
    }

    public RetTypeAnalyser(BytecodeVars varAnalyser) {
        this.varAnalyser = varAnalyser;
    }

    @Override
    public ClassReference visitAssignExpr(Expr.Assign expr) {
        return varAnalyser.getType(expr.name().lexeme());
    }

    @Override
    public ClassReference visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        return varAnalyser.getType(expr.name().lexeme());
    }

    @Override
    public ClassReference visitBinaryExpr(Expr.Binary expr) {
        return expr.retType();
    }

    @Override
    public ClassReference visitWhenExpr(Expr.When expr) {
        return findRetType(expr.ifTrue());
    }

    @Override
    public ClassReference visitInstCallExpr(Expr.InstCall expr) {
        return expr.retType();
    }

    @Override
    public ClassReference visitStaticCallExpr(Expr.StaticCall expr) {
        return expr.retType();
    }

    @Override
    public ClassReference visitGetExpr(Expr.Get expr) {
        if (expr.type().get().isArray()) return VarTypeManager.INTEGER.reference();
        return expr.type().get().getFieldType(expr.name().lexeme());
    }

    @Override
    public ClassReference visitStaticGetExpr(Expr.StaticGet expr) {
        return expr.target() == null ? VarTypeManager.VOID.reference() : expr.target().get().getFieldType(expr.name().lexeme());
    }

    @Override
    public ClassReference visitArrayGetExpr(Expr.ArrayGet expr) {
        return findRetType(expr.object()).get().getComponentType().reference();
    }

    @Override
    public ClassReference visitSetExpr(Expr.Set expr) {
        return findRetType(expr.object()).get().getFieldType(expr.name().lexeme());
    }

    @Override
    public ClassReference visitStaticSetExpr(Expr.StaticSet expr) {
        return expr.target().get().getFieldType(expr.name().lexeme());
    }

    @Override
    public ClassReference visitArraySetExpr(Expr.ArraySet expr) {
        return findRetType(expr.object()).get().getComponentType().reference();
    }

    @Override
    public ClassReference visitSpecialSetExpr(Expr.SpecialSet expr) {
        return findRetType(expr.callee()).get().getFieldType(expr.name().lexeme());
    }

    @Override
    public ClassReference visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        return expr.target().get().getFieldType(expr.name().lexeme());
    }

    @Override
    public ClassReference visitArraySpecialExpr(Expr.ArraySpecial expr) {
        return findRetType(expr.object()).get().getComponentType().reference();
    }

    @Override
    public ClassReference visitSliceExpr(Expr.Slice expr) {
        return findRetType(expr.object());
    }

    @Override
    public ClassReference visitSwitchExpr(Expr.Switch expr) {
        return findRetType(expr.defaulted());
    }

    @Override
    public ClassReference visitCastCheckExpr(Expr.CastCheck expr) {
        return VarTypeManager.BOOLEAN.reference();
    }

    @Override
    public ClassReference visitGroupingExpr(Expr.Grouping expr) {
        return findRetType(expr.expression());
    }

    @Override
    public ClassReference visitLiteralExpr(Expr.Literal expr) {
        return expr.literal().literal().type().reference();
    }

    @Override
    public ClassReference visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
        return expr.compoundType().array();
    }

    @Override
    public ClassReference visitLogicalExpr(Expr.Logical expr) {
        return VarTypeManager.BOOLEAN.reference();
    }

    @Override
    public ClassReference visitSuperCallExpr(Expr.SuperCall expr) {
        return expr.retType();
    }

    @Override
    public ClassReference visitUnaryExpr(Expr.Unary expr) {
        return findRetType(expr.right());
    }

    @Override
    public ClassReference visitVarRefExpr(Expr.VarRef expr) {
        return varAnalyser.getType(expr.name().lexeme());
    }

    @Override
    public ClassReference visitConstructorExpr(Expr.Constructor expr) {
        return expr.target();
    }
}
