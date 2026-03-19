package net.kapitencraft.scripted.edit.graphical.code;

import net.kapitencraft.scripted.edit.graphical.widgets.block.StmtCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import org.jetbrains.annotations.Nullable;

public interface CodeParser {
    //TODO

    static Expr parseExpr(ExprCodeWidget widget) {
        return widget.getType().parse(widget);
    }

    static Stmt parseStmtList(StmtCodeWidget widget) {
        return null;
    }

    static Stmt parseOptionalStmtList(@Nullable StmtCodeWidget widget) {
        if (widget == null)
            return null;
        return parseStmtList(widget);
    }
}
