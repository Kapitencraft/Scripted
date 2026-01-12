package net.kapitencraft.scripted.edit.graphical.inserter.block;

import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;

public interface BlockGhostInserter extends GhostInserter {

    void insertChildMiddle(BlockCodeWidget widget);
}