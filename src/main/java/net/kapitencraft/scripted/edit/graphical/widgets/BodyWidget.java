package net.kapitencraft.scripted.edit.graphical.widgets;

import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.IRenderable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class BodyWidget implements CodeWidget {
    private final List<CodeWidget> children;

    public BodyWidget(IRenderable renderable) {
        this.children = RenderHelper.decompileVisualText(renderable);
    }

    public BodyWidget(List<CodeWidget> children) {
        this.children = children;
    }

    public BodyWidget(CodeWidget... children) {
        this.children = List.of(children);
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, renderX, renderY, 6 + getWidth(font), 22);
        RenderHelper.renderExprList(graphics, font, renderX + 4, renderY + 7, children);
    }

    @Override
    public int getWidth(Font font) {
        return this.children.stream().mapToInt(w -> w.getWidth(font)).sum();
    }

    @Override
    public int getHeight() {
        return Math.max(19, this.children.stream().mapToInt(CodeWidget::getHeight).max().orElse(19));
    }
}
