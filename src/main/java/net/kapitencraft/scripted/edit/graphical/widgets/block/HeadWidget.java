package net.kapitencraft.scripted.edit.graphical.widgets.block;

import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class HeadWidget extends BlockWidget {
    private final List<CodeWidget> expr;

    public HeadWidget(List<CodeWidget> expr) {
        this.expr = expr;
    }

    public HeadWidget(BlockWidget widget, List<CodeWidget> expr) {
        this.expr = expr;
        this.setChild(widget);
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(CodeWidgetSprites.METHOD_HEAD, renderX, renderY, getWidth(font), 3 + getHeight());
        RenderHelper.renderExprList(graphics, font, renderX + 4, renderY + 15, this.expr);
        super.render(graphics, font, renderX, renderY);
    }

    @Override
    public int getWidth(Font font) {
        return 6 + this.expr.stream().mapToInt(w -> w.getWidth(font)).sum();
    }

    @Override
    public int getHeight() {
        return Math.max(27, 17 + CodeWidget.getHeightFromList(this.expr));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < 8) return WidgetFetchResult.notRemoved(this, x, y);
        if (y > this.getHeight()) return this.fetchChildRemoveHovered(x, y - this.getHeight(), font);
        if (x < this.getWidth(font)) return WidgetFetchResult.fromExprList(4, x, y, font, this, this.expr, false);
        return null;
    }

    public static class Builder implements BlockWidget.Builder<HeadWidget> {
        private final List<CodeWidget> expr = new ArrayList<>();
        private BlockWidget child;

        public Builder withExpr(CodeWidget widget) {
            expr.add(widget);
            return this;
        }

        public Builder setChild(BlockWidget widget) {
            this.child = widget;
            return this;
        }

        @Override
        public HeadWidget build() {
            return new HeadWidget(child, expr);
        }
    }
}
