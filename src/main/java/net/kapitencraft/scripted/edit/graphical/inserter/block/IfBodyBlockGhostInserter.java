package net.kapitencraft.scripted.edit.graphical.inserter.block;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.IfWidget;

public class IfBodyBlockGhostInserter implements BlockGhostInserter {
    private final IfWidget owner;

    public IfBodyBlockGhostInserter(IfWidget owner) {
        this.owner = owner;
    }

    @Override
    public void insert(CodeWidget target) {
        this.owner.setBody((BlockCodeWidget) target);
    }

    @Override
    public void insertChildMiddle(BlockCodeWidget ghostElement) {
        this.owner.insertBodyMiddle(ghostElement);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IfBodyBlockGhostInserter bGH && bGH.owner == this.owner;
    }
}
