package net.kapitencraft.scripted.lang.compiler.analyser;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.holder.ast.ElifBranch;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.ast.SwitchKey;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FinalsPopulatedAnalyser implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final ErrorStorage errorStorage;

    public FinalsPopulatedAnalyser(ErrorStorage errorStorage) {
        this.errorStorage = errorStorage;
    }

    public void apply(List<String> requireValue) {
        for (String s : requireValue) {
            this.states.put(s, Status.NOT_ASSIGNED);
        }
    }

    private void errorAssigned(Token name) {
        error(name, "can not write to final field");
    }

    private void error(Token loc, String s) {
        errorStorage.error(loc, s);
    }

    private enum Status {
        ASSIGNED,
        NOT_ASSIGNED
    }

    //TODO add scopes
    private final Map<String, Status> states = new LinkedHashMap<>(); //ensure order equals creation order

    private void analyseIfNN(Expr value) {
        if (value != null) {
            analyse(value);
        }
    }

    private void analyse(Expr expr) {
        expr.accept(this);
    }

    private void analyse(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitSingleIdentifierExpr(Expr.SingleIdentifier expr) {
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        //TODO
        String lexeme = expr.name.lexeme();
        if (states.get(lexeme) == Status.ASSIGNED)
            errorAssigned(expr.name);
        states.put(lexeme, Status.ASSIGNED);
        analyse(expr.value);
        analyse(expr.object);
        return null;
    }

    @Override
    public Void visitArraySpecialExpr(Expr.ArraySpecial expr) {
        analyse(expr.object);
        analyse(expr.index);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        analyse(expr.object);
        for (Expr arg : expr.args) {
            analyse(arg);
        }
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        analyse(expr.left);
        analyse(expr.right);
        return null;
    }

    @Override
    public Void visitComparisonChainExpr(Expr.ComparisonChain expr) {
        for (Expr entry : expr.entries) {
            analyse(entry);
        }
        return null;
    }

    @Override
    public Void visitCastCheckExpr(Expr.CastCheck expr) {
        analyse(expr.object);
        return null;
    }

    @Override
    public Void visitArrayGetExpr(Expr.ArrayGet expr) {
        analyse(expr.object);
        analyse(expr.index);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
        if (expr.size != null)
            analyse(expr.size);
        else {
            for (Expr arg : expr.obj) {
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
        String name = expr.name.lexeme();
        if (states.get(name) == Status.ASSIGNED) {
            errorAssigned(expr.name);
        } else {
            error(expr.name, "can not read unassigned final field");
        }
        analyse(expr.object);
        return null;
    }

    @Override
    public Void visitArraySetExpr(Expr.ArraySet expr) {
        analyse(expr.object);
        analyse(expr.value);
        analyse(expr.index);
        return null;
    }

    @Override
    public Void visitIdentifierSpecialAssignExpr(Expr.IdentifierSpecialAssign expr) {
        if (expr.type != null && !expr.isStatic) {
            String name = expr.name.lexeme();
            if (states.get(name) == Status.ASSIGNED) {
                errorAssigned(expr.name);
            }
        }
        return null;
    }

    @Override
    public Void visitConstructorExpr(Expr.Constructor expr) {
        for (Expr arg : expr.args) {
            analyse(arg);
        }
        return null;
    }

    @Override
    public Void visitStaticSetExpr(Expr.StaticSet expr) {
        analyse(expr.value);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        analyse(expr.right);
        return null;
    }

    @Override
    public Void visitWhenExpr(Expr.When expr) {
        analyse(expr.condition);
        analyse(expr.ifTrue);
        analyse(expr.ifFalse);
        return null;
    }

    @Override
    public Void visitStaticGetExpr(Expr.StaticGet expr) {
        //doesn't need to do anything
        return null;
    }

    @Override
    public Void visitSwitchExpr(Expr.Switch expr) {
        analyse(expr.provider);
        for (SwitchKey param : expr.params) {
            analyse(param.expr);
        }
        analyseIfNN(expr.defaulted);
        return null;
    }

    @Override
    public Void visitSliceExpr(Expr.Slice expr) {
        analyse(expr.object);
        analyseIfNN(expr.start);
        analyseIfNN(expr.end);
        analyseIfNN(expr.interval);
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        analyse(expr.object);
        return null;
    }

    @Override
    public Void visitIdentifierAssignExpr(Expr.IdentifierAssign expr) {
        analyse(expr.value);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        analyse(expr.left);
        analyse(expr.right);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        analyseIfNN(stmt.value);
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        analyse(stmt.init);
        analyse(stmt.condition);
        analyseIfNN(stmt.increment);
        analyse(stmt.body);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        analyse(stmt.condition);
        analyse(stmt.body);
        return null;
    }

    @Override
    public Void visitForEachStmt(Stmt.ForEach stmt) {
        analyse(stmt.initializer);
        analyse(stmt.body);
        return null;
    }

    @Override
    public Void visitDebugTraceStmt(Stmt.DebugTrace stmt) {
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        analyse(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarDeclStmt(Stmt.VarDecl stmt) {
        if (stmt.initializer != null)
            analyse(stmt.initializer);
        return null;
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        for (Stmt statement : stmt.statements) {
            analyse(statement);
        }
        return null;
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        analyse(stmt.body);
        for (Pair<Pair<ClassReference[], Token>, Stmt.Block> aCatch : stmt.catches) {
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
        analyse(stmt.condition);
        analyse(stmt.thenBranch);
        for (ElifBranch branch : stmt.elifs) {
            analyse(branch.condition);
            analyse(branch.body);
        }
        if (stmt.elseBranch != null)
            analyse(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        return null;
    }
}
