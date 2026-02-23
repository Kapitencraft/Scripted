package net.kapitencraft.scripted.edit.graphical.code;

import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.lang.holder.ast.Expr;

public interface ExprCodeVisitor {

    Expr parse(ExprCodeWidget widget);

    ExprCodeWidget decode(Expr expr);
}
