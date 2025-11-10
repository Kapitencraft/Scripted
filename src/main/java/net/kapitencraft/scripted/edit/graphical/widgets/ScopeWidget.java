package net.kapitencraft.scripted.edit.graphical.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class ScopeWidget implements CodeWidget {
    private final List<CodeWidget> widgets;

    public ScopeWidget(List<CodeWidget> widgets) {
        this.widgets = widgets;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY, int textX, int textY) {
        for (CodeWidget widget : this.widgets) {
            widget.render(graphics, font, renderX, renderY, textX, textY);
            int height = widget.getHeight();
            renderY += height;
            textY += height;
        }
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }
}
