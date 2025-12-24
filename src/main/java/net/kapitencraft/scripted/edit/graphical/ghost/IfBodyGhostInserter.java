package net.kapitencraft.scripted.edit.graphical.ghost;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.IfWidget;

public class IfBodyGhostInserter implements GhostInserter {
    private final IfWidget owner;

    public IfBodyGhostInserter(IfWidget owner) {
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
        return obj instanceof IfBodyGhostInserter bGH && bGH.owner == this.owner;
    }
}
