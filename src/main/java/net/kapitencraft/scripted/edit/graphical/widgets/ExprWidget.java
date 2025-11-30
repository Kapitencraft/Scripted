package net.kapitencraft.scripted.edit.graphical.widgets;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.ExprType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class ExprWidget implements CodeWidget, Removable {
    public static final MapCodec<ExprWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExprType.CODEC.fieldOf("expr_type").forGetter(w -> w.type),
            CodeWidget.CODEC.listOf().fieldOf("children").forGetter(w -> w.children)
    ).apply(i, ExprWidget::new));

    private final ExprType type;
    private final List<CodeWidget> children;

    public ExprWidget(ExprType type, List<CodeWidget> children) {
        this.type = type;
        this.children = new ArrayList<>(children);
    }

    @Override
    public Type getType() {
        return Type.EXPR;
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

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (x > this.getWidth(font)) return null;
        return WidgetFetchResult.fromExprList(6, x, y, font, this, this.children);
    } //TODO fix offset
}
