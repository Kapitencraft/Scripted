package net.kapitencraft.scripted.edit.graphical.code;

import net.kapitencraft.scripted.edit.graphical.widgets.block.IfWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.StmtCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.WhileLoopWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.BinaryOperationWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.GetVarWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ParamWidget;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GraphicalConverter implements Expr.Visitor<ExprCodeWidget>, Stmt.Visitor<StmtCodeWidget> {
    public ExprCodeWidget convert(Expr expr) {
        return expr == null ? ParamWidget.OBJ : expr.accept(this);
    }

    public StmtCodeWidget convert(Stmt stmt) {
        return stmt == null ? null : stmt.accept(this);
    }

    @Override
    public ExprCodeWidget visitVarRefExpr(Expr.VarRef expr) {
        return new GetVarWidget(expr.name().lexeme());
    }

    @Override
    public ExprCodeWidget visitSetExpr(Expr.Set expr) {
        return new ;
    }

    @Override
    public ExprCodeWidget visitArraySpecialExpr(Expr.ArraySpecial expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitRegistryAccessExpr(Expr.RegistryAccess expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitInstCallExpr(Expr.InstCall expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitLogicalExpr(Expr.Logical expr) {
        return new BinaryOperationWidget(
                convert(expr.left()),
                expr.operator().type(),
                convert(expr.right())
        );
    }

    @Override
    public ExprCodeWidget visitSuperCallExpr(Expr.SuperCall expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitComparisonChainExpr(Expr.ComparisonChain expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitCastCheckExpr(Expr.CastCheck expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitArrayGetExpr(Expr.ArrayGet expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitSpecialSetExpr(Expr.SpecialSet expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitArraySetExpr(Expr.ArraySet expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitConstructorExpr(Expr.Constructor expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitStaticSetExpr(Expr.StaticSet expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitGroupingExpr(Expr.Grouping expr) {
        return convert(expr.expression());
    }

    @Override
    public ExprCodeWidget visitUnaryExpr(Expr.Unary expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitWhenExpr(Expr.When expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitStaticGetExpr(Expr.StaticGet expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitSwitchExpr(Expr.Switch expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitSliceExpr(Expr.Slice expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitGetExpr(Expr.Get expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitAssignExpr(Expr.Assign expr) {
        return new ;
    }

    @Override
    public ExprCodeWidget visitStaticCallExpr(Expr.StaticCall expr) {
        return null;
    }

    @Override
    public ExprCodeWidget visitBinaryExpr(Expr.Binary expr) {
        return new BinaryOperationWidget(
                convert(expr.left()),
                expr.operator().type(),
                convert(expr.right())
        );
    }

    @Override
    public StmtCodeWidget visitReturnStmt(Stmt.Return stmt) {
        return null;
    }

    @Override
    public StmtCodeWidget visitForStmt(Stmt.For stmt) {
        return null;
    }

    @Override
    public StmtCodeWidget visitWhileStmt(Stmt.While stmt) {
        return new WhileLoopWidget(
                convert(stmt.condition()),
                convert(stmt.body())
        );
    }

    @Override
    public StmtCodeWidget visitForEachStmt(Stmt.ForEach stmt) {
        return null;
    }

    @Override
    public StmtCodeWidget visitDebugTraceStmt(Stmt.DebugTrace stmt) {
        return null;
    }

    @Override
    public StmtCodeWidget visitExpressionStmt(Stmt.Expression stmt) {
        return null;
    }

    @Override
    public StmtCodeWidget visitVarDeclStmt(Stmt.VarDecl stmt) {
        return null;
    }

    @Override
    public StmtCodeWidget visitThrowStmt(Stmt.Throw stmt) {
        return null;
    }

    @Override
    public StmtCodeWidget visitBlockStmt(Stmt.Block stmt) {
        Stmt[] stmts = stmt.statements();
        StmtCodeWidget sCW = convert(stmts[0]);
        for (int i = 1; i < stmts.length; i++) {
            sCW.setBottomChild(convert(stmts[i]));
        }
        return sCW;
    }

    @Override
    public StmtCodeWidget visitTryStmt(Stmt.Try stmt) {
        return null;
    }

    @Override
    public StmtCodeWidget visitClearLocalsStmt(Stmt.ClearLocals stmt) {
        return null;
    }

    @Override
    public StmtCodeWidget visitIfStmt(Stmt.If stmt) {
        List<IfWidget.ElseIfEntry> list = Arrays.stream(stmt.elifs()).map(elifBranch -> new IfWidget.ElseIfEntry(
                convert(elifBranch.condition()),
                convert(elifBranch.body())
        )).collect(Collectors.toCollection(ArrayList::new));

        return new IfWidget(
                null,
                convert(stmt.condition()),
                convert(stmt.thenBranch()),
                convert(stmt.elseBranch()),
                stmt.elseBranch() != null,
                list
        );
    }

    @Override
    public StmtCodeWidget visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        return null;
    }
}
