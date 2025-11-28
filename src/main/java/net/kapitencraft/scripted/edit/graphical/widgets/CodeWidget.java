package net.kapitencraft.scripted.edit.graphical.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public interface CodeWidget {

    Type getType();

    void render(GuiGraphics graphics, Font font, int renderX, int renderY);

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

    static int getHeightFromList(List<CodeWidget> widgets) {
        return widgets.stream().mapToInt(CodeWidget::getHeight).max().orElse(0);
    }

    static int getWidthFromList(Font font, List<CodeWidget> widgets) {
        return widgets.stream().mapToInt(w -> w.getWidth(font)).sum();
    }
}
