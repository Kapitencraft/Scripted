package net.kapitencraft.scripted.edit.graphical.inserter.block;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;

public interface BlockGhostInserter {

    void insert(BlockCodeWidget target);

    void insertChildMiddle(BlockCodeWidget widget);

    boolean equals(Object other);
}