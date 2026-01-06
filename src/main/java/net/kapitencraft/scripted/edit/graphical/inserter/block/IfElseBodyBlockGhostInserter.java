package net.kapitencraft.scripted.edit.graphical.inserter.block;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.IfWidget;

public class IfElseBodyBlockGhostInserter implements BlockGhostInserter {
    private final IfWidget owner;

    public IfElseBodyBlockGhostInserter(IfWidget owner) {
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
        return obj instanceof IfElseBodyBlockGhostInserter bGH && bGH.owner == this.owner;
    }
}
