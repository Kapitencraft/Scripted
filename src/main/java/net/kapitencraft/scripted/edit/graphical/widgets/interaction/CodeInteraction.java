package net.kapitencraft.scripted.edit.graphical.widgets.interaction;

public abstract class CodeInteraction {
    private final int x, y;
    private final int width, height;

    protected CodeInteraction(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void onClick(int mouseX, int mouseY);

    public final void onClickRelative(int posX, int posY) {
        this.onClick(posX - x, posY - y);
    }

    public boolean hovered(int posX, int posY) {
        return x < posX && x + width > posX && y < posY && y + height > posY;
    }
}