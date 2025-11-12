package net.kapitencraft.scripted.edit.graphical.widgets;

import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.ExprType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class ExprWidget implements CodeWidget {
    private final ExprType type;
    private final List<CodeWidget> children;

    public ExprWidget(ExprType type, List<CodeWidget> children) {
        this.type = type;
        this.children = children;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(type.getSpriteLocation(), renderX, renderY - getHeight() / 2 + 4, getWidth(font), getHeight());
        RenderHelper.renderExprList(graphics, font, renderX + 6, renderY, this.children);
    }

    @Override
    public int getWidth(Font font) {
        return CodeWidget.getWidthFromList(font, this.children) + 12;
    }

    @Override
    public int getHeight() {
        return CodeWidget.getHeightFromList(this.children) + 4;
    }
}
