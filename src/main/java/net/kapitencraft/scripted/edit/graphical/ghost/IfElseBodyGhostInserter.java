package net.kapitencraft.scripted.edit.graphical.ghost;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.IfWidget;

public class IfElseBodyGhostInserter implements GhostInserter {
    private final IfWidget owner;

    public IfElseBodyGhostInserter(IfWidget owner) {
        this.owner = owner;
    }

    @Override
    public void insert(BlockWidget target) {
        owner.setElseBody(target);
    }

    @Override
    public void insertChildMiddle(BlockWidget widget) {
        this.owner.insertElseMiddle(widget);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IfElseBodyGhostInserter bGH && bGH.owner == this.owner;
    }
}
