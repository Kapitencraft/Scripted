package net.kapitencraft.scripted.edit.graphical.inserter.expr;

import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;

public interface ExprGhostInserter extends GhostInserter {

    ExprCodeWidget getOriginal();
}
