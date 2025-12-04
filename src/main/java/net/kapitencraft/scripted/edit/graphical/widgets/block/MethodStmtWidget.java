package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.ExprWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MethodStmtWidget extends BlockWidget {
    public static final MapCodec<MethodStmtWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i)
                    .and(Codec.STRING.fieldOf("signature").forGetter(w -> w.signature))
                    .and(Codec.unboundedMap(Codec.STRING, CodeWidget.CODEC).fieldOf("args").forGetter(w -> w.arguments))
                    .apply(i, MethodStmtWidget::new)
    );

    private final String signature;
    private final Map<String, CodeWidget> arguments;

    public MethodStmtWidget(Optional<BlockWidget> child, String signature, Map<String, CodeWidget> arguments) {
        this.signature = signature;
        this.arguments = arguments;
        child.ifPresent(this::setChild);
    }

    public MethodStmtWidget(BlockWidget child, String sig, Map<String, CodeWidget> arguments) {
        this.setChild(child);
        this.signature = sig;
        this.arguments = arguments;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Type getType() {
        return Type.METHOD_STMT;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {

        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 7, signature, arguments);
    }

    @Override
    public int getWidth(Font font) {
        return RenderHelper.getVisualTextWidth(font, this.signature, this.arguments) + 12;
    }

    @Override
    public int getHeight() {
        return CodeWidget.getHeightFromArgs(this.arguments) + 4;
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }

    public static class Builder implements BlockWidget.Builder<MethodStmtWidget> {
        private BlockWidget child = null;
        private String signature = null;
        private final Map<String, CodeWidget> arguments = new HashMap<>();

        public Builder setChild(BlockWidget child) {
            this.child = child;
            return this;
        }

        public Builder setChild(BlockWidget.Builder<?> child) {
            return this.setChild(child.build());
        }

        public Builder setSignature(String signature) {
            this.signature = signature;
            return this;
        }

        public Builder withArgument(String name, CodeWidget child) {
            this.arguments.put(name, child);
            return this;
        }

        @Override
        public MethodStmtWidget build() {
            return new MethodStmtWidget(child, Objects.requireNonNull(signature, "missing signature"), arguments);
        }
    }
}
