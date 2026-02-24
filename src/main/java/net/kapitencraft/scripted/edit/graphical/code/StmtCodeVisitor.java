package net.kapitencraft.scripted.edit.graphical.code;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;

public interface StmtCodeVisitor<W extends BlockCodeWidget, S extends Stmt> {

    S parse(W widget);

    W decode(S stmt);
}
