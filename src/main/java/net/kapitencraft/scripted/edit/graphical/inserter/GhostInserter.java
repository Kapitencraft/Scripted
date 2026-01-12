package net.kapitencraft.scripted.edit.graphical.inserter;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;

public interface GhostInserter {
    void insert(CodeWidget target);

    boolean equals(Object other);
}
