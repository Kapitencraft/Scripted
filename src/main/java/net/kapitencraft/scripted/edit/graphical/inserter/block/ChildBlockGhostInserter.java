package net.kapitencraft.scripted.edit.graphical.inserter.block;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockWidget;

public class ChildBlockGhostInserter implements BlockGhostInserter {
    private final BlockWidget owner;

    public ChildBlockGhostInserter(BlockWidget owner) {
        this.owner = owner;
    }

    @Override
    public void insert(BlockWidget target) {
        this.owner.setChild(target);
    }

    @Override
    public void insertChildMiddle(BlockWidget ghostElement) {
        this.owner.insertChildMiddle(ghostElement);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ChildBlockGhostInserter cGI && cGI.owner == this.owner;
    }
}
