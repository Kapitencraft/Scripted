package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.ArgumentStorage;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class HeadWidget extends BlockCodeWidget {
    public static final MapCodec<HeadWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockCodeWidget.CODEC.optionalFieldOf("child").forGetter(w -> Optional.ofNullable(w.getChild())),
            Codec.STRING.fieldOf("translationKey").forGetter(w -> w.translationKey),
            Codec.unboundedMap(Codec.STRING, ExprCodeWidget.CODEC).fieldOf("children").forGetter(w -> w.args)
    ).apply(i, HeadWidget::new));

    private final String translationKey;
    private final Map<String, ExprCodeWidget> args = new HashMap<>();

    public HeadWidget(Optional<BlockCodeWidget> child, String translationKey, Map<String, ExprCodeWidget> map) {
        this.translationKey = translationKey;
        this.args.putAll(map);
        child.ifPresent(this::setChild);
    }

    public HeadWidget(BlockCodeWidget child, String translationKey, Map<String, ExprCodeWidget> map) {
        this.translationKey = translationKey;
        this.args.putAll(map);
        this.setChild(child);
    }

    public HeadWidget(BlockCodeWidget widget, String translationKey) {
        this.translationKey = translationKey;
        this.setChild(widget);
    }

    @Override
    public BlockCodeWidget copy() {
        return new HeadWidget(
                getChildCopy(),
                this.translationKey
        );
    }

    @Override
    public void update(@Nullable MethodContext context) {
        MethodContext nC = new MethodContext();
        this.args.values().forEach(w -> w.update(nC));
        super.update(nC);
    }

    @Override
    protected @NotNull Type getType() {
        return Type.HEAD;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(CodeWidgetSprites.METHOD_HEAD, renderX, renderY, getWidth(font), 3 + getHeight());
        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 15, this.translationKey, this.args);
        super.render(graphics, font, renderX, renderY);
    }

    @Override
    public int getWidth(Font font) {
        return 6 + RenderHelper.getVisualTextWidth(font, this.translationKey, this.args);
    }

    @Override
    public int getHeight() {
        return Math.max(27, 17 + ExprCodeWidget.getHeightFromArgs(this.args));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < 8) return BlockWidgetFetchResult.notRemoved(this, x, y);
        if (y > this.getHeight()) return super.fetchAndRemoveHovered(x, y - this.getHeight(), font);
        if (x < this.getWidth(font))
            return BlockWidgetFetchResult.fromExprList(4, x, y, font, this, this.translationKey, ArgumentStorage.create(this.args));
        return null;
    }

    public static class Builder implements BlockCodeWidget.Builder<HeadWidget> {
        private String translationKey;
        private final Map<String, ExprCodeWidget> expr = new HashMap<>();
        private BlockCodeWidget child;

        public Builder withExpr(String argName, ExprCodeWidget widget) {
            expr.put(argName, widget);
            return this;
        }

        public Builder setTranslationKey(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public Builder setChild(BlockCodeWidget widget) {
            this.child = widget;
            return this;
        }

        @Override
        public HeadWidget build() {
            return new HeadWidget(child, Objects.requireNonNull(translationKey, "missing translation key!"), expr);
        }
    }
}
