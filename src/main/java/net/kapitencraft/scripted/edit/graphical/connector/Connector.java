package net.kapitencraft.scripted.edit.graphical.connector;

import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import net.minecraft.client.gui.GuiGraphics;

public abstract class Connector {
    private final int x, y;
    private MethodContext.Snapshot contextSnapshot;

    protected Connector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setContextSnapshot(MethodContext.Snapshot contextSnapshot) {
        this.contextSnapshot = contextSnapshot;
    }

    public abstract void insert(BlockCodeWidget widget);

    public abstract BlockCodeWidget get();

    public void renderDebug(GuiGraphics graphics) {
        graphics.fill(x + 2, y - 1, x + 22, y + 1, 0x8FFF0000);
    }

    public boolean canConnect(int x, int y) {
    }
}
