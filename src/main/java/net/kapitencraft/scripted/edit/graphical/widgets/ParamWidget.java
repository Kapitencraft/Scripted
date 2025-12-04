package net.kapitencraft.scripted.edit.graphical.widgets;

import net.kapitencraft.scripted.edit.graphical.ExprType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class ParamWidget implements CodeWidget {
    private final ExprType type;

    public ParamWidget(ExprType exprType) {
        this.type = exprType;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(type.getSpriteLocation(), renderX, renderY, 14, 12);
    }

    @Override
    public int getWidth(Font font) {
        return 14;
    }

    @Override
    public int getHeight() {
        return 12;
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }
}
