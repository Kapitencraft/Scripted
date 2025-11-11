package net.kapitencraft.scripted.edit.graphical.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class ScopeWidget implements CodeWidget {
    private final List<CodeWidget> widgets;

    public ScopeWidget(List<CodeWidget> widgets) {
        this.widgets = widgets;
    }

    public ScopeWidget(CodeWidget... widgets) {
        this.widgets = List.of(widgets);
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        for (CodeWidget widget : this.widgets) {
            widget.render(graphics, font, renderX, renderY);
            int height = widget.getHeight();
            renderY += height;
        }
    }

    @Override
    public int getWidth(Font font) {
        return this.widgets.stream().mapToInt(w -> w.getWidth(font)).max().orElse(0);
    }

    @Override
    public int getHeight() {
        return this.widgets.stream().mapToInt(CodeWidget::getHeight).sum();
    }
}
