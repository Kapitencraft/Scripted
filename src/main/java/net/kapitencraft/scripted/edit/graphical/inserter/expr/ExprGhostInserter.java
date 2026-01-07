package net.kapitencraft.scripted.edit.graphical.inserter.expr;

import net.kapitencraft.scripted.edit.graphical.widgets.ExprCodeWidget;

public interface ExprGhostInserter {

    void insert(ExprCodeWidget target);

    boolean equals(Object other);
}
