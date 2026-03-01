package net.kapitencraft.scripted.lang.compiler.analyser;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.holder.ast.ElifBranch;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;

import java.util.LinkedHashMap;
import java.util.Map;

public class FinalsPopulatedAnalyser implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private enum Status {
        ASSIGNED,
        NOT_ASSIGNED
    }

    //TODO add scopes
    private final Map<String, Status> states = new LinkedHashMap<>(); //ensure order equals creation order

    private void analyse(Expr expr) {
        expr.accept(this);
    }

    private void analyse(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitVarRefExpr(Expr.VarRef expr) {
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        //TODO
        analyse(expr.value());
        analyse(expr.object());
        return null;
    }

    @Override
    public Void visitArraySpecialExpr(Expr.ArraySpecial expr) {
        analyse(expr.object());
        analyse(expr.index());
        return null;
    }

    @Override
    public Void visitInstCallExpr(Expr.InstCall expr) {
        analyse(expr.callee());
        for (Expr arg : expr.args()) {
            analyse(arg);
        }
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        analyse(expr.left());
        analyse(expr.right());
        return null;
    }

    @Override
    public Void visitSuperCallExpr(Expr.SuperCall expr) {
        analyse(expr.callee());
        for (Expr arg : expr.args()) {
            analyse(arg);
        }
        return null;
    }

    @Override
    public Void visitCastCheckExpr(Expr.CastCheck expr) {
        analyse(expr.object());
        return null;
    }

    @Override
    public Void visitArrayGetExpr(Expr.ArrayGet expr) {
        analyse(expr.object());
        analyse(expr.index());
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
        if (expr.size() != null)
            analyse(expr.size());
        else {
            for (Expr arg : expr.obj()) {
                analyse(arg);
            }
        }
        return null;
    }

    @Override
    public Void visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        return null;
    }

    @Override
    public Void visitSpecialSetExpr(Expr.SpecialSet expr) {
        analyse(expr.callee());
        return null;
    }

    @Override
    public Void visitArraySetExpr(Expr.ArraySet expr) {
        analyse(expr.object());
        analyse(expr.value());
        analyse(expr.index());
        return null;
    }

    @Override
    public Void visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        return null;
    }

    @Override
    public Void visitConstructorExpr(Expr.Constructor expr) {
        for (Expr arg : expr.args()) {
            analyse(arg);
        }
        return null;
    }

    @Override
    public Void visitStaticSetExpr(Expr.StaticSet expr) {
        analyse(expr.value());
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        analyse(expr.expression());
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        analyse(expr.right());
        return null;
    }

    @Override
    public Void visitWhenExpr(Expr.When expr) {
        analyse(expr.condition());
        analyse(expr.ifTrue());
        analyse(expr.ifFalse());
        return null;
    }

    @Override
    public Void visitStaticGetExpr(Expr.StaticGet expr) {
        return null;
    }

    @Override
    public Void visitSwitchExpr(Expr.Switch expr) {
        analyse(expr.provider());
        expr.params().values().forEach(this::analyse);
        return null;
    }

    @Override
    public Void visitSliceExpr(Expr.Slice expr) {
        analyse(expr.object());
        if (expr.start() != null)
            analyse(expr.start());
        if (expr.end() != null)
            analyse(expr.end());
        if (expr.interval() != null)
            analyse(expr.interval());
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        analyse(expr.object());
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        analyse(expr.value());
        return null;
    }

    @Override
    public Void visitStaticCallExpr(Expr.StaticCall expr) {
        for (Expr arg : expr.args()) {
            analyse(arg);
        }
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        analyse(expr.left());
        analyse(expr.right());
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        analyse(stmt.init());
        analyse(stmt.condition());
        analyse(stmt.increment());
        analyse(stmt.body());
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        analyse(stmt.condition());
        analyse(stmt.body());
        return null;
    }

    @Override
    public Void visitForEachStmt(Stmt.ForEach stmt) {
        analyse(stmt.initializer());
        analyse(stmt.body());
        return null;
    }

    @Override
    public Void visitDebugTraceStmt(Stmt.DebugTrace stmt) {
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        analyse(stmt.expression());
        return null;
    }

    @Override
    public Void visitVarDeclStmt(Stmt.VarDecl stmt) {
        if (stmt.initializer() != null)
            analyse(stmt.initializer());
        return null;
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        for (Stmt statement : stmt.statements()) {
            analyse(statement);
        }
        return null;
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        analyse(stmt.body());
        for (Pair<Pair<ClassReference[], Token>, Stmt.Block> aCatch : stmt.catches()) {
            analyse(aCatch.getSecond());
        }
        return null;
    }

    @Override
    public Void visitClearLocalsStmt(Stmt.ClearLocals stmt) {
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        analyse(stmt.condition());
        analyse(stmt.thenBranch());
        for (ElifBranch branch : stmt.elifs()) {
            analyse(branch.condition());
            analyse(branch.body());
        }
        if (stmt.elseBranch() != null)
            analyse(stmt.elseBranch());
        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        return null;
    }
}
