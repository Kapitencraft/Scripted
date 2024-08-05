package net.kapitencraft.scripted.edit.graphical.render;

import net.minecraft.client.gui.GuiGraphics;

public abstract class IRenderer {
    protected int x, y;

    abstract void render(GuiGraphics graphics);


}
