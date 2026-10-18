package net.kapitencraft.scripted.edit.graphical.widgets;

import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;

public interface ArgumentStorage {

    void remove(String name);

    ExprCodeWidget get(String name);
}
