package net.kapitencraft.scripted.edit.graphical.inserter.block;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.WhileLoopWidget;

public class WhileBodyBlockGhostInserter implements BlockGhostInserter {
    private final WhileLoopWidget owner;

    public WhileBodyBlockGhostInserter(WhileLoopWidget owner) {
        this.owner = owner;
    }

    @Override
    public void insert(BlockCodeWidget target) {
        this.owner.setBody(target);
    }

    @Override
    public void insertChildMiddle(BlockCodeWidget widget) {
        this.owner.insertBodyMiddle(widget);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WhileBodyBlockGhostInserter bGH && bGH.owner == this.owner;
    }
}
