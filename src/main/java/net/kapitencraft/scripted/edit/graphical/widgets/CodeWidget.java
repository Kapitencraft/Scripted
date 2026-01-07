package net.kapitencraft.scripted.edit.graphical.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public interface CodeWidget {

    void render(GuiGraphics graphics, Font font, int renderX, int renderY);

    int getHeight();

    int getWidth(Font font);

    CodeWidget copy();
}
