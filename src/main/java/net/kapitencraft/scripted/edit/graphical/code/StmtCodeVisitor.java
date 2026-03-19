package net.kapitencraft.scripted.edit.graphical.code;

import net.kapitencraft.scripted.edit.graphical.widgets.block.StmtCodeWidget;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;

public interface StmtCodeVisitor<W extends StmtCodeWidget, S extends Stmt> {

    S parse(W widget);
}
