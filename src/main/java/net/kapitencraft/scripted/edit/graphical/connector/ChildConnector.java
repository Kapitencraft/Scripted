package net.kapitencraft.scripted.edit.graphical.connector;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;

public class ChildConnector extends Connector {
    private final BlockCodeWidget target;

    public ChildConnector(int aX, int aY, BlockCodeWidget target) {
        super(aX, aY + target.getHeight());
        this.target = target;
    }

    @Override
    public void insert(BlockCodeWidget widget) {
        this.target.setChild(widget);
    }

    @Override
    public BlockCodeWidget get() {
        return target.getChild();
    }
}
