package net.kapitencraft.scripted.edit.graphical.code;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;

public interface StmtCodeVisitor<T extends BlockCodeWidget> {

    Stmt parse(T widget);

    T decode(Stmt stmt);
}
