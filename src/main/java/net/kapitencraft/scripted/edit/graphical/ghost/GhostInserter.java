package net.kapitencraft.scripted.edit.graphical.ghost;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockWidget;

public interface GhostInserter {

    void insert(BlockWidget target);

    void insertChildMiddle(BlockWidget widget);

    boolean equals(Object other);
}
