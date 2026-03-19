package net.kapitencraft.scripted.edit.graphical.connector;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.StmtCodeWidget;
import org.jetbrains.annotations.Nullable;

public class ChildBlockConnector extends BlockConnector {
    private final StmtCodeWidget target;

    public ChildBlockConnector(int aX, int aY, StmtCodeWidget target) {
        super(aX, aY + target.getHeight());
        this.target = target;
    }

    @Override
    public void insert(@Nullable CodeWidget widget) {
        this.target.setChild((StmtCodeWidget) widget);
    }

    @Override
    public StmtCodeWidget get() {
        return target.getChild();
    }
}
