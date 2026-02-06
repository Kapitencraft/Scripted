package net.kapitencraft.scripted.edit.graphical.connector;

import net.minecraft.client.gui.GuiGraphics;

public abstract class ExprConnector extends Connector {

    protected ExprConnector(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean canConnect(int xO, int yO, int xM, int yM) {
        return xM > xO + this.x - 6 && xM < xO + this.x + 6 &&
                yM > yO + this.y - 10 && yM < yO + this.y + 20;
    }

    @Override
    public void renderDebug(GuiGraphics graphics) {
        graphics.fill(x - 1, y - 5, x + 1, y + 15, 0x8FFF0000);
    }
}
