package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.fetch.ExprWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.kapitencraft.scripted.edit.graphical.inserter.expr.ArgumentInserter;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

//TODO removing expression crashes the game
public class ExprWidget implements ExprCodeWidget {
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
    public GhostInserter getGhostWidgetTarget(int x, int y, Font font, boolean isBlock) {
        if (isBlock) return null;
        return ArgumentInserter.create(x, y, font, translationKey, args::put, args);
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(type.getSpriteLocation(), renderX, renderY, getWidth(font), getHeight());
        int height = getHeight();
        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 5 + (height - 18) / 2, this.translationKey, this.args);
    }

    @Override
    public int getWidth(Font font) {
        return RenderHelper.getVisualTextWidth(font, this.translationKey, this.args) + 12;
    }

    @Override
    public int getHeight() {
        return Math.max(18, ExprCodeWidget.getHeightFromArgs(this.args) + 4);
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (x > this.getWidth(font)) return null;
        return ExprWidgetFetchResult.fromExprList(4, x, y, font, this, this.translationKey, this.args);
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        RenderHelper.registerAllInteractions(xOrigin, yOrigin, font, sink, translationKey, args);
    }

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

    @Override
    public void update(@Nullable MethodContext context) {
        this.args.values().forEach(c -> c.update(context));
    }
}
