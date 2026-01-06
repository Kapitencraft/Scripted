package net.kapitencraft.scripted.edit.graphical.inserter.expr;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;

public interface ExprGhostInserter {

    void insert(CodeWidget target);

    boolean equals(Object other);
}
