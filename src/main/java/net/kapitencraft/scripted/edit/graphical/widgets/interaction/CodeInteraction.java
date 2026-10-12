package net.kapitencraft.scripted.edit.graphical.widgets.interaction;

import net.minecraft.client.gui.GuiGraphics;

public abstract class CodeInteraction {
    protected final int x, y;
    private final int width, height;

    protected CodeInteraction(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void onClick(int mouseX, int mouseY, InteractionData callbacks);

    public final void onClickRelative(int posX, int posY, InteractionData callbacks) {
        this.onClick(posX - x, posY - y, callbacks);
    }

    public boolean hovered(int posX, int posY) {
        return x < posX && x + width > posX && y < posY && y + height > posY;
    }

    public void renderDebug(GuiGraphics pGuiGraphics) {
        pGuiGraphics.fill(x, y, x + width, y + height, 0x8F0000FF);
    }
}