package net.kapitencraft.scripted.edit.graphical.connector;

import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public abstract class Connector {
    protected final int x, y;
    private MethodContext.Snapshot contextSnapshot;

    protected Connector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setContextSnapshot(MethodContext.Snapshot contextSnapshot) {
        this.contextSnapshot = contextSnapshot;
    }

    public abstract void insert(@Nullable CodeWidget widget);

    public abstract CodeWidget get();

    public abstract void renderDebug(GuiGraphics graphics);

    public abstract boolean canConnect(int xO, int yO, int xM, int yM);
}
