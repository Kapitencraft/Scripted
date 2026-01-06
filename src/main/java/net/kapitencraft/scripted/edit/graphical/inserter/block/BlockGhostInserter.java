package net.kapitencraft.scripted.edit.graphical.inserter.block;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockWidget;

public interface BlockGhostInserter {

    void insert(BlockWidget target);

    void insertChildMiddle(BlockWidget widget);

    boolean equals(Object other);
}