package net.kapitencraft.scripted.edit.graphical.widgets.stmt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.CodeWidgetHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.widgets.ArgumentStorage;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.connector.ArgumentExprConnector;
import net.kapitencraft.scripted.edit.graphical.widgets.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class CompoundStmtWidget extends StmtCodeWidget {
    static final MapCodec<CompoundStmtWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i).and(
                            StmtCodeWidget.CODEC.fieldOf("target").forGetter(cSW -> cSW.target)
                    ).and(
                            Codec.STRING.fieldOf("translation").forGetter(cSW -> cSW.translationKey)
                    ).and(
                            Codec.unboundedMap(Codec.STRING, ExprCodeWidget.CODEC).fieldOf("args").forGetter(cSW -> cSW.args)
                    ).apply(i, CompoundStmtWidget::new));

    private final StmtCodeWidget target;
    private final String translationKey;
    private final Map<String, ExprCodeWidget> args = new HashMap<>();

    private CompoundStmtWidget(Optional<StmtCodeWidget> child, StmtCodeWidget target, String translationKey, Map<String, ExprCodeWidget> args) {
        child.ifPresent(this::setChild);
        this.target = target;
        this.translationKey = translationKey;
        this.args.putAll(args);
    }

    public CompoundStmtWidget(@Nullable StmtCodeWidget child, StmtCodeWidget target, String translationKey, Map<String, ExprCodeWidget> args) {
        this.setChild(child);
        this.target = target;
        this.translationKey = translationKey;
        this.args.putAll(args);
    }

    @Override
    protected @NotNull Type getType() {
        return Type.COMPOUND;
    }

    @Override
    public int getWidth(Font font) {
        return CodeWidgetHelper.getVisualTextWidth(font, translationKey, args) + 12;
    }

    @Override
    public int getHeight() {
        return Math.max(18, ExprCodeWidget.getHeightFromArgs(this.args) + 4);
    }

    @Override
    public StmtCodeWidget copy() {
        return new CompoundStmtWidget(
                this.getChildCopy(),
                this.target,
                this.translationKey,
                new HashMap<>(this.args)
        );
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int height = getHeight();
        graphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, renderX, renderY, getWidth(font), 3 + height);
        CodeWidgetHelper.renderVisualText(graphics, font, renderX, renderY + 7 + (getHeight() - 20) / 2, translationKey, args);
        super.render(graphics, font, renderX, renderY);
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        if (this.args.containsKey(arg)) {
            this.args.put(arg, obj);
        } else
            throw new IllegalArgumentException("unknown argument in compound for '" + this.translationKey + "': " + arg);
    }

    @Override
    public CodeWidget getByName(String arg) {
        if (this.args.containsKey(arg))
            return this.args.get(arg);
        throw new IllegalArgumentException("unknown argument in compound for '" + this.translationKey + "': " + arg);
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        ArgumentExprConnector.parse(font, aX + 4, aY, translationKey, args, getHeight(), this, collector);
        super.collectConnectors(aX, aY, font, collector);
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y > this.getHeight()) return super.fetchAndRemoveHovered(x, y - this.getHeight(), font);
        if (x < this.getWidth(font))
            return BlockWidgetFetchResult.fromExprList(4, x, y, font, this, translationKey,
                    ArgumentStorage.create(this.args)
            );
        return null;
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        CodeWidgetHelper.registerAllInteractions(xOrigin, yOrigin + 7 + (getHeight() - 20) / 2, font, sink, translationKey, args);
        super.registerInteractions(xOrigin, yOrigin, font, sink);
    }

    public static class Builder {
        private StmtCodeWidget target, child;
        private String key;
        private final Map<String, ExprCodeWidget> args = new HashMap<>();

        public Builder setTarget(StmtCodeWidget target) {
            this.target = target;
            return this;
        }

        public Builder setChild(StmtCodeWidget child) {
            this.child = child;
            return this;
        }

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder addArgument(String key, ExprCodeWidget child) {
            this.args.put(key, child);
            return this;
        }

        public CompoundStmtWidget build() {
            return new CompoundStmtWidget(
                    child,
                    target,
                    key,
                    args
            );
        }
    }
}
