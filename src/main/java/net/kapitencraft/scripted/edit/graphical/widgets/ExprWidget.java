package net.kapitencraft.scripted.edit.graphical.widgets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ExprWidget implements ExprCodeWidget, Removable {
    public static final MapCodec<ExprWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExprCategory.CODEC.fieldOf("category").forGetter(w -> w.type),
            Codec.STRING.fieldOf("translationKey").forGetter(w -> w.translationKey),
            Codec.unboundedMap(Codec.STRING, ExprCodeWidget.CODEC).fieldOf("args").forGetter(w -> w.args)
    ).apply(i, ExprWidget::new));

    private final ExprCategory type;
    private final String translationKey;
    private final Map<String, ExprCodeWidget> args = new HashMap<>();

    public ExprWidget(ExprCategory type, String translationKey, Map<String, ExprCodeWidget> args) {
        this.type = type;
        this.translationKey = translationKey;
        this.args.putAll(args);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public @NotNull Type getType() {
        return Type.EXPR;
    }

    @Override
    public ExprCodeWidget copy() {
        return new ExprWidget(this.type, this.translationKey, this.args);
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(type.getSpriteLocation(), renderX, renderY - getHeight() / 2 + 3, getWidth(font), getHeight());
        RenderHelper.renderVisualText(graphics, font, renderX + 6, renderY, this.translationKey, this.args);
    }

    @Override
    public int getWidth(Font font) {
        return RenderHelper.getVisualTextWidth(font, this.translationKey, this.args) + 12;
    }

    @Override
    public int getHeight() {
        return Math.max(19, ExprCodeWidget.getHeightFromArgs(this.args) + 4);
    }

    @Override
    public BlockWidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (x > this.getWidth(font)) return null;
        return BlockWidgetFetchResult.fromExprList(6, x, y, font, this, this.translationKey, this.args);
    } //TODO fix offset

    public static class Builder implements ExprCodeWidget.Builder<ExprWidget> {
        private ExprCategory type;
        private String translationKey;
        private final Map<String, ExprCodeWidget> args = new HashMap<>();

        public Builder setTranslationKey(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public Builder setType(ExprCategory type) {
            this.type = type;
            return this;
        }

        public Builder withParam(String argName, ExprCodeWidget entry) {
            this.args.put(argName, entry);
            return this;
        }

        public Builder withParam(String argName, ExprCodeWidget.Builder<?> builder) {
            return this.withParam(argName, builder.build());
        }

        @Override
        public ExprWidget build() {
            return new ExprWidget(type, translationKey, args);
        }
    }
}
