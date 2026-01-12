package net.kapitencraft.scripted.edit.graphical.inserter.block;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;

public class ChildBlockGhostInserter implements BlockGhostInserter {
    private final BlockCodeWidget owner;

    public ChildBlockGhostInserter(BlockCodeWidget owner) {
        this.owner = owner;
    }

    @Override
    public void insert(CodeWidget target) {
        this.owner.setChild((BlockCodeWidget) target);
    }

    @Override
    public void insertChildMiddle(BlockCodeWidget ghostElement) {
        this.owner.insertChildMiddle(ghostElement);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ChildBlockGhostInserter cGI && cGI.owner == this.owner;
    }
}
