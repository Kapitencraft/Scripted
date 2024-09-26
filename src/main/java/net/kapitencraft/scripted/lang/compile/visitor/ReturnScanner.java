package net.kapitencraft.scripted.lang.compile.visitor;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compile.Compiler;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;

import java.util.List;

public class ReturnScanner implements Stmt.Visitor<Boolean> {
    private final Compiler.ErrorLogger errorLogger;

    public ReturnScanner(Compiler.ErrorLogger errorLogger) {
        this.errorLogger = errorLogger;
    }

    public boolean scanList(List<Stmt> stmts) {
        boolean seenReturn = false;
        for (Stmt stmt1 : stmts) {
            if (seenReturn) errorLogger.error(stmt1, "unreachable statement");
            if (scanReturn(stmt1)) {
                seenReturn = true;
            }
        }
        return seenReturn;
    }

    public boolean scanReturn(Stmt stmt) {
        return stmt.accept(this);
    }

    @Override
    public Boolean visitBlockStmt(Stmt.Block stmt) {
        return scanList(stmt.statements);
    }

    @Override
    public Boolean visitExpressionStmt(Stmt.Expression stmt) {
        return false;
    }

    @Override
    public Boolean visitFuncDeclStmt(Stmt.FuncDecl stmt) {
        return false;
    }

    @Override
    public Boolean visitIfStmt(Stmt.If stmt) {
        boolean seenReturn = true;
        seenReturn &= scanReturn(stmt.thenBranch);
        seenReturn &= stmt.elifs.stream().map(Pair::getSecond).allMatch(this::scanReturn);
        seenReturn &= scanReturn(stmt.elseBranch);

        return seenReturn;
    }

    @Override
    public Boolean visitReturnStmt(Stmt.Return stmt) {
        return true;
    }

    @Override
    public Boolean visitVarDeclStmt(Stmt.VarDecl stmt) {
        return false;
    }

    @Override
    public Boolean visitWhileStmt(Stmt.While stmt) {
        return scanReturn(stmt.body);
    }

    @Override
    public Boolean visitForStmt(Stmt.For stmt) {
        return scanReturn(stmt.body);
    }

    @Override
    public Boolean visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        return false;
    }
}
