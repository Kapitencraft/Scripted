package net.kapitencraft.scripted.edit.graphical.inserter.block;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.IfWidget;

public class IfElseBodyBlockGhostInserter implements BlockGhostInserter {
    private final IfWidget owner;

    public IfElseBodyBlockGhostInserter(IfWidget owner) {
        this.owner = owner;
    }

    @Override
    public void insert(BlockCodeWidget target) {
        owner.setElseBody(target);
    }

    @Override
    public void insertChildMiddle(BlockCodeWidget widget) {
        this.owner.insertElseMiddle(widget);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IfElseBodyBlockGhostInserter bGH && bGH.owner == this.owner;
    }
}
