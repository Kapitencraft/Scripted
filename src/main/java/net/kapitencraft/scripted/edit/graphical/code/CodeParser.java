package net.kapitencraft.scripted.edit.graphical.code;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import org.jetbrains.annotations.Nullable;

public interface CodeParser {
    //TODO

    static Expr parseExpr(ExprCodeWidget widget) {
        return null;
    }

    static Stmt parseStmtList(BlockCodeWidget widget) {
        return null;
    }

    static Stmt parseOptionalStmtList(@Nullable BlockCodeWidget widget) {
        if (widget == null)
            return null;
        return parseStmtList(widget);
    }

    static ExprCodeWidget decodeExpr(Expr condition) {
        return null;
    }

    static BlockCodeWidget decodeStmtList(Stmt body) {
        return null;
    }

    static @Nullable BlockCodeWidget decodeOptionalStmtList(Stmt stmt) {
        if (stmt == null)
            return null;
        return decodeStmtList(stmt);
    }
}
