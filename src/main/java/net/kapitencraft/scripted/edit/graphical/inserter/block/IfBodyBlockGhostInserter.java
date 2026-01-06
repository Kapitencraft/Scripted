package net.kapitencraft.scripted.edit.graphical.inserter.block;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.IfWidget;

public class IfBodyBlockGhostInserter implements BlockGhostInserter {
    private final IfWidget owner;

    public IfBodyBlockGhostInserter(IfWidget owner) {
        this.owner = owner;
    }

    @Override
    public void insert(BlockWidget target) {
        this.owner.setBody(target);
    }

    @Override
    public void insertChildMiddle(BlockWidget ghostElement) {
        this.owner.insertBodyMiddle(ghostElement);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IfBodyBlockGhostInserter bGH && bGH.owner == this.owner;
    }
}
