package net.kapitencraft.scripted.edit.client.block_element;

import net.kapitencraft.scripted.edit.client.Renderables;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class TextElement extends BlockElement {
    private final String text;

    public TextElement(String text) {
        this.text = text;
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY, int textX, int textY) {
        int width = font.width(text);
        for (int i = 0; i < width; i+=2) {
            Renderables.BLOCK_MIDDLE.render(graphics, renderX + i, renderY);
        }
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }
}
