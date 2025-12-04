package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.*;

public class HeadWidget extends BlockWidget {
    public static final MapCodec<HeadWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("translationKey").forGetter(w -> w.translationKey),
            Codec.unboundedMap(Codec.STRING, CodeWidget.CODEC).fieldOf("children").forGetter(w -> w.children)
    ).apply(i, HeadWidget::new));

    private final String translationKey;
    private final Map<String, CodeWidget> children = new HashMap<>();

    public HeadWidget(String translationKey, Map<String, CodeWidget> map) {
        this.translationKey = translationKey;
        this.children.putAll(map);
    }

    public HeadWidget(BlockWidget widget, String translationKey) {
        this.translationKey = translationKey;
        this.setChild(widget);
    }

    @Override
    public Type getType() {
        return Type.HEAD;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(CodeWidgetSprites.METHOD_HEAD, renderX, renderY, getWidth(font), 3 + getHeight());
        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 15, this.translationKey, this.children);
        super.render(graphics, font, renderX, renderY);
    }

    @Override
    public int getWidth(Font font) {
        return 6 + RenderHelper.getVisualTextWidth(font, this.translationKey, this.children);
    }

    @Override
    public int getHeight() {
        return Math.max(27, 17 + CodeWidget.getHeightFromArgs(this.children));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < 8) return WidgetFetchResult.notRemoved(this, x, y);
        if (y > this.getHeight()) return this.fetchChildRemoveHovered(x, y - this.getHeight(), font);
        if (x < this.getWidth(font))
            return WidgetFetchResult.fromExprList(4, x, y, font, this, this.translationKey, this.children, false);
        return null;
    }

    public static class Builder implements BlockWidget.Builder<HeadWidget> {
        private String translationKey;
        private final Map<String, CodeWidget> expr = new HashMap<>();
        private BlockWidget child;

        public Builder withExpr(String argName, CodeWidget widget) {
            expr.put(argName, widget);
            return this;
        }

        public Builder setTranslationKey(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public Builder setChild(BlockWidget widget) {
            this.child = widget;
            return this;
        }

        @Override
        public HeadWidget build() {
            return new HeadWidget(Objects.requireNonNull(translationKey, "missing translation key!"), expr);
        }
    }
}
