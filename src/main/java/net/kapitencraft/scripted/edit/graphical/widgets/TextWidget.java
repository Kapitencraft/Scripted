package net.kapitencraft.scripted.edit.graphical.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class TextWidget implements CodeWidget {
    private final String text;

    public TextWidget(String text) {
        this.text = text;
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY, int textX, int textY) {
        graphics.drawString(font, text, textX, textY, 0, false);
    }

    @Override
    public int getWidth(Font font) {
        return font.width(this.text);
    }

    @Override
    public int getHeight() {
        return 10;
    }
}
