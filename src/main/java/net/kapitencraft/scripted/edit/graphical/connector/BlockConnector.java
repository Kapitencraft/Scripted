package net.kapitencraft.scripted.edit.graphical.connector;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import net.minecraft.client.gui.GuiGraphics;

public abstract class BlockConnector extends Connector {
    protected BlockConnector(int x, int y) {
        super(x, y);
    }

    @Override
    public BlockCodeWidget get() {
        return null;
    }

    @Override
    public void renderDebug(GuiGraphics graphics) {
        graphics.fill(x + 2, y - 1, x + 22, y + 1, 0x8FFF0000);
    }

    @Override
    public boolean canConnect(int xO, int yO, int xM, int yM) {
        return xM > xO + this.x - 3 && xM < xO + this.x + 27 &&
                yM > yO + this.y - 6 && yM < yO + this.y + 6;
    }
}
