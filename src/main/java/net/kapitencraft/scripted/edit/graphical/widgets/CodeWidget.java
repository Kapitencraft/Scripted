package net.kapitencraft.scripted.edit.graphical.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public interface CodeWidget {

    Type getType();

    void render(GuiGraphics graphics, Font font, int renderX, int renderY, int textX, int textY);

    int getWidth(Font font);

    int getHeight();

    enum Type {
        START,
        TEXT,
        BODY,
        VAR_BOOL,
        VAR_PRIMITIVE,
        VAR_OTHER,
        END
    }
}
