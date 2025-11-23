package net.kapitencraft.scripted.edit.graphical.widgets.block;

import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.IRenderable;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.TextWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class BodyWidget extends BlockWidget {
    private final List<CodeWidget> expr;

    public BodyWidget(IRenderable renderable) {
        this.expr = RenderHelper.decompileVisualText(renderable);
    }

    public BodyWidget(List<CodeWidget> expr) {
        this.expr = expr;
    }

    public BodyWidget(CodeWidget... expr) {
        this.expr = List.of(expr);
    }

    private BodyWidget(BlockWidget child, List<CodeWidget> expr) {
        this.expr = expr;
        this.setChild(child);
    }

    public static Builder text(String enclosed) {
        return builder().withExpr(new TextWidget(enclosed));
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int height = getHeight();
        graphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, renderX, renderY, 6 + getWidth(font), 3 + height);
        RenderHelper.renderExprList(graphics, font, renderX + 4, renderY + 7 + Math.max(0, (height - 19)) / 2, expr);
        super.render(graphics, font, renderX, renderY);
    }

    @Override
    public int getWidth(Font font) {
        return this.expr.stream().mapToInt(w -> w.getWidth(font)).sum();
    }

    @Override
    public int getHeight() {
        return Math.max(19, 9 + CodeWidget.getHeightFromList(this.expr));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y > this.getHeight()) return fetchChildRemoveHovered(x, y - this.getHeight(), font);
        if (x < this.getWidth(font)) return WidgetFetchResult.fromExprList(4, x, y, font, this, this.expr);
        return null;
    }

    public static class Builder implements BlockWidget.Builder<BodyWidget> {
        private BlockWidget child;
        private final List<CodeWidget> expr = new ArrayList<>();

        @Override
        public BodyWidget build() {
            return new BodyWidget(child, expr);
        }

        public Builder withExpr(CodeWidget value) {
            this.expr.add(value);
            return this;
        }

        public Builder setChild(BlockWidget.Builder<?> builder) {
            this.child = builder.build();
            return this;
        }
    }
}
