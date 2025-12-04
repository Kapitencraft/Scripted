package net.kapitencraft.scripted.edit.graphical.widgets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.ExprType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExprWidget implements CodeWidget, Removable {
    public static final MapCodec<ExprWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExprType.CODEC.fieldOf("exprType").forGetter(w -> w.type),
            Codec.STRING.fieldOf("translationKey").forGetter(w -> w.translationKey),
            Codec.unboundedMap(Codec.STRING, CodeWidget.CODEC).fieldOf("args").forGetter(w -> w.args)
    ).apply(i, ExprWidget::new));

    private final ExprType type;
    private final String translationKey;
    private final Map<String, CodeWidget> args = new HashMap<>();

    public ExprWidget(ExprType type, String translationKey, Map<String, CodeWidget> args) {
        this.type = type;
        this.translationKey = translationKey;
        this.args.putAll(args);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Type getType() {
        return Type.EXPR;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(type.getSpriteLocation(), renderX, renderY - getHeight() / 2 + 4, getWidth(font), getHeight());
        RenderHelper.renderVisualText(graphics, font, renderX + 6, renderY, this.translationKey, this.args);
    }

    @Override
    public int getWidth(Font font) {
        return RenderHelper.getVisualTextWidth(font, this.translationKey, this.args) + 12;
    }

    @Override
    public int getHeight() {
        return CodeWidget.getHeightFromArgs(this.args) + 4;
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (x > this.getWidth(font)) return null;
        return WidgetFetchResult.fromExprList(6, x, y, font, this, this.translationKey, this.args);
    } //TODO fix offset

    public static class Builder implements CodeWidget.Builder<ExprWidget> {
        private ExprType type;
        private String translationKey;
        private final Map<String, CodeWidget> args = new HashMap<>();

        public Builder setTranslationKey(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public Builder setType(ExprType type) {
            this.type = type;
            return this;
        }

        public Builder withParam(String argName, CodeWidget entry) {
            this.args.put(argName, entry);
            return this;
        }

        public Builder withParam(String argName, CodeWidget.Builder<?> builder) {
            return this.withParam(argName, builder.build());
        }

        @Override
        public ExprWidget build() {
            return new ExprWidget(type, translationKey, args);
        }
    }
}
