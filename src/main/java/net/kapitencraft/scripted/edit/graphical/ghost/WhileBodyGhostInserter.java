package net.kapitencraft.scripted.edit.graphical.ghost;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.WhileLoopWidget;

public class WhileBodyGhostInserter implements GhostInserter {
    private final WhileLoopWidget owner;

    public WhileBodyGhostInserter(WhileLoopWidget owner) {
        this.owner = owner;
    }

    @Override
    public void insert(BlockWidget target) {
        this.owner.setBody(target);
    }

    @Override
    public void insertChildMiddle(BlockWidget widget) {
        this.owner.insertBodyMiddle(widget);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WhileBodyGhostInserter bGH && bGH.owner == this.owner;
    }
}
