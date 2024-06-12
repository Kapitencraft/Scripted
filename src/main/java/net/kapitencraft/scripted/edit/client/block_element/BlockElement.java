package net.kapitencraft.scripted.edit.client.block_element;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public abstract class BlockElement {

    public abstract Type getType();

    public abstract void render(GuiGraphics graphics, Font font, int renderX, int renderY, int textX, int textY);

    public abstract int getWidth(Font font);

    public enum Type {
        START,
        TEXT,
        BODY,
        VAR_BOOL,
        VAR_PRIMITIVE,
        VAR_OTHER,
        END
    }
}
