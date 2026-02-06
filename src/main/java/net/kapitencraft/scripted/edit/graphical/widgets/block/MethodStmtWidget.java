package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

//the difference to ExprCodeWidget?
//idk
public class MethodStmtWidget extends BlockCodeWidget {
    public static final MapCodec<MethodStmtWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i)
                    .and(Codec.STRING.fieldOf("signature").forGetter(w -> w.signature))
                    .and(Codec.unboundedMap(Codec.STRING, ExprCodeWidget.CODEC).fieldOf("args").forGetter(w -> w.args))
                    .apply(i, MethodStmtWidget::new)
    );

    private final String signature;
    private final Map<String, ExprCodeWidget> args = new HashMap<>();

    public MethodStmtWidget(Optional<BlockCodeWidget> child, String signature, Map<String, ExprCodeWidget> args) {
        this.signature = signature;
        this.args.putAll(args);
        child.ifPresent(this::setChild);
    }

    public MethodStmtWidget(BlockCodeWidget child, String sig, Map<String, ExprCodeWidget> args) {
        this.setChild(child);
        this.signature = sig;
        this.args.putAll(args);
    }

    @Override
    public BlockCodeWidget copy() {
        return new MethodStmtWidget(
                this.getChildCopy(),
                this.signature,
                this.args
        );
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        if (args.containsKey(arg)) {
            args.put(arg, obj);
        } else {
            throw new IllegalArgumentException("unknown argument in expr '" + this.signature + "': " + arg);
        }
    }

    @Override
    public CodeWidget getByName(String argName) {
        return args.get(argName);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected @NotNull Type getType() {
        return Type.METHOD_STMT;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {

        int height = getHeight();
        graphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, renderX, renderY, 6 + getWidth(font), 3 + height);
        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 7, signature, args);
    }

    @Override
    public int getWidth(Font font) {
        return RenderHelper.getVisualTextWidth(font, this.signature, this.args) + 12;
    }

    @Override
    public int getHeight() {
        return Math.max(19, ExprCodeWidget.getHeightFromArgs(this.args) + 4);
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return x < this.getWidth(font) ? BlockWidgetFetchResult.notRemoved(this, x, y) : super.fetchAndRemoveHovered(x, y, font);
    }

    @Override
    public void update(@Nullable MethodContext context) {
        this.args.values().forEach(c -> c.update(context));
        super.update(context);
    }

    public static class Builder implements BlockCodeWidget.Builder<MethodStmtWidget> {
        private BlockCodeWidget child = null;
        private String signature = null;
        private final Map<String, ExprCodeWidget> arguments = new HashMap<>();

        public Builder setChild(BlockCodeWidget child) {
            this.child = child;
            return this;
        }

        public Builder setChild(BlockCodeWidget.Builder<?> child) {
            return this.setChild(child.build());
        }

        public Builder setSignature(String signature) {
            this.signature = signature;
            return this;
        }

        public Builder withArgument(String name, ExprCodeWidget child) {
            this.arguments.put(name, child);
            return this;
        }

        @Override
        public MethodStmtWidget build() {
            return new MethodStmtWidget(child, Objects.requireNonNull(signature, "missing signature"), arguments);
        }
    }
}
