package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.kap_lib.core.collection.MapStream;
import net.kapitencraft.scripted.edit.CodeWidgetHelper;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.ArgumentExprConnector;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.ExprWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ExprWidget implements ExprCodeWidget {
    public static final MapCodec<ExprWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExprCategory.CODEC.fieldOf("category").forGetter(w -> w.type),
            Codec.STRING.fieldOf("translationKey").forGetter(w -> w.translationKey),
            Codec.unboundedMap(Codec.STRING, ExprCodeWidget.CODEC).fieldOf("args").forGetter(w -> w.args)
    ).apply(i, ExprWidget::new));

    private int x, y;
    private final ExprCategory type;
    private final String translationKey;
    private final Map<String, ExprCodeWidget> args = new HashMap<>();
    private ExprWidget child;

    public ExprWidget(ExprCategory type, String translationKey, Map<String, ExprCodeWidget> args) {
        this.type = type;
        this.translationKey = translationKey;
        this.args.putAll(args);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public @NotNull Type getType() {
        return Type.EXPR;
    }

    @Override
    public ExprWidget copy() {
        ExprWidget exprWidget = new ExprWidget(this.type, this.translationKey, MapStream.of(this.args).mapValues(ExprCodeWidget::copy).toMap());
        if (this.child != null)
            exprWidget.setChild(this.child.copy());
        return exprWidget;
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        if (args.containsKey(arg)) {
            args.put(arg, obj);
        } else {
            throw new IllegalArgumentException("unknown argument in expr '" + this.translationKey + "': " + arg);
        }
    }

    @Override
    public CodeWidget getByName(String arg) {
        return args.get(arg);
    }

    @Override
    public void collectConnectors(Font font, Consumer<Connector> collector) {
        ArgumentExprConnector.parse(font, x + 4, y, this.translationKey, this.args, this.getHeight(), this, collector);
        //collector.accept(new ExprChainConnector(aX + 4 + this.getWidth(font), aY, this));
    }

    @Override
    public void render(GuiGraphics graphics, Font font) {
        int renderX = this.x;
        int renderY = this.y;
        int height = getHeight();
        graphics.blitSprite(type.getSpriteLocation(), renderX, renderY, getWidth(font), height);
        CodeWidgetHelper.renderVisualText(graphics, font, renderX, renderY, this.translationKey, this.args);
    }

    @Override
    public int getWidth(Font font) {
        return CodeWidgetHelper.getVisualTextWidth(font, this.translationKey, this.args) + 12;
    }

    @Override
    public int getHeight() {
        return Math.max(18, ExprCodeWidget.getHeightFromArgs(this.args) + 4);
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (x > this.getWidth(font)) {
            if (this.child != null)
                return this.child.fetchAndRemoveHovered(x + this.getWidth(font), y, font);
            return null;
        }
        return ExprWidgetFetchResult.fromExprList(4, x, y, font, this, this.translationKey, this.args);
    }

    @Override
    public void registerInteractions(Font font, Consumer<CodeInteraction> sink) {
        CodeWidgetHelper.registerAllInteractions(font, sink, args);
    }

    public void setChild(@Nullable ExprWidget codeWidget) {
        this.child = codeWidget;
    }

    public @Nullable ExprWidget getChild() {
        return this.child;
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
    public void update(@Nullable MethodContext context, Font font, int x, int y) {
        CodeWidgetHelper.updateVisualText(context, font, x, y, this.translationKey, this.args);
    }
}
