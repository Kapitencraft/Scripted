package net.kapitencraft.scripted.edit.graphical.connector;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import org.jetbrains.annotations.Nullable;

public class ChildBlockConnector extends BlockConnector {
    private final BlockCodeWidget target;

    public ChildBlockConnector(int aX, int aY, BlockCodeWidget target) {
        super(aX, aY + target.getHeight());
        this.target = target;
    }

    @Override
    public void insert(@Nullable CodeWidget widget) {
        this.target.setChild((BlockCodeWidget) widget);
    }

    @Override
    public BlockCodeWidget get() {
        return target.getChild();
    }
}
