package net.kapitencraft.scripted.edit.graphical.ghost;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockWidget;

public class ChildGhostInserter implements GhostInserter {
    private final BlockWidget owner;

    public ChildGhostInserter(BlockWidget owner) {
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
        return obj instanceof ChildGhostInserter cGI && cGI.owner == this.owner;
    }
}
