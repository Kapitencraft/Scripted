package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.kap_lib.core.stream.Functions;
import net.kapitencraft.scripted.edit.CodeWidgetHelper;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.connector.SingletonExprConnector;
import net.kapitencraft.scripted.edit.graphical.fetch.ExprWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractBinaryOperationWidget<O extends StringRepresentable> implements ExprCodeWidget {
    public static <T extends AbstractBinaryOperationWidget<O>, O extends StringRepresentable> MapCodec<T> codec(Codec<O> operationCodec, Supplier<ExprCodeWidget> fallback, O operationFallback, Functions.F3<ExprCodeWidget, O, ExprCodeWidget, T> constructor) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                ExprCodeWidget.CODEC.fieldOf("left").orElseGet(fallback).forGetter(w -> w.left),
                operationCodec.optionalFieldOf("operation", operationFallback).forGetter(w -> w.operatorWidget.getValue()),
                ExprCodeWidget.CODEC.fieldOf("right").orElseGet(fallback).forGetter(w -> w.right)
        ).apply(i, constructor::apply));
    }

    protected int x, y;

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

    protected ExprCodeWidget left;
    protected final EnumSelectionWidget<O> operatorWidget;
    protected ExprCodeWidget right;
    private final ResourceLocation sprite;

    protected AbstractBinaryOperationWidget(ExprCodeWidget left, O[] values, O operation, ExprCodeWidget right, ResourceLocation sprite) {
        this.left = left;
        this.operatorWidget = new EnumSelectionWidget<>(List.of(values), StringRepresentable::getSerializedName);
        this.sprite = sprite;
        this.operatorWidget.set(operation);
        this.right = right;
    }

    @Override
    public void render(GuiGraphics graphics, Font font) {
        graphics.blitSprite(sprite, this.x, this.y, getWidth(font), getHeight());
        this.left.render(graphics, font);
        this.right.render(graphics, font);
        this.operatorWidget.render(graphics, font);
    }

    @Override
    public int getWidth(Font font) {
        return 6 + CodeWidgetHelper.getVisualTextWidth(font, "§bin_op", Map.of("left", left, "op", this.operatorWidget, "right", right));
    }

    @Override
    public int getHeight() {
        return Math.max(18, ExprCodeWidget.getHeightFromEntries(List.of(left, right)) + 4);
    }

    @Override
    public void update(@Nullable MethodContext context, Font font, int x, int y) {
        this.x = x;
        this.y = y;
        this.left.update(context, font, x, y);
        int spaceWidth = font.width(" ");
        x += this.left.getWidth(font) + spaceWidth;
        this.operatorWidget.update(context, font, x, y);
        x += this.operatorWidget.getWidth(font) + spaceWidth;
        this.right.update(context, font, x, y);
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        int spaceWidth = font.width(" ");
        int oX = x;
        if (x < 4)
            return ExprWidgetFetchResult.notRemoved(this, x, y);
        x -= 4;
        int leftWidth = this.left.getWidth(font);
        if (x < leftWidth) {
            WidgetFetchResult result = this.left.fetchAndRemoveHovered(x, y, font);
            if (result == null)
                return ExprWidgetFetchResult.notRemoved(this, oX, y);
            if (!result.removed())
                left = ParamWidget.NUM.get();
            return result.setRemoved();
        }
        x -= leftWidth + spaceWidth + this.operatorWidget.getWidth(font);
        int rightWidth = this.right.getWidth(font);
        if (x < rightWidth) {
            WidgetFetchResult result = this.right.fetchAndRemoveHovered(x, y, font);
            if (result == null)
                return ExprWidgetFetchResult.notRemoved(this, oX, y);
            if (!result.removed())
                right = ParamWidget.NUM.get();
            return result.setRemoved();
        }
        return ExprWidgetFetchResult.notRemoved(this, oX, y);
    }

    @Override
    public void registerInteractions(Font font, Consumer<CodeInteraction> sink) {
        this.left.registerInteractions(font, sink);
        this.operatorWidget.registerInteractions(font, sink);
        this.right.registerInteractions(font, sink);
    }

    @Override
    public void collectConnectors(Font font, Consumer<Connector> collector) {
        int aX = this.x;
        int aY = this.y;
        int spaceWidth = font.width(" ");
        int connectorOffset = aY + 6 + (getHeight() - 20) / 2;
        aX += 4;
        collector.accept(new SingletonExprConnector(
                aX,
                connectorOffset - (left.getHeight() - 8) / 2,
                w -> this.left = w,
                () -> this.left
        ));
        this.left.collectConnectors(font, collector);
        aX += this.left.getWidth(font) + 2 * spaceWidth + this.operatorWidget.getWidth(font);
        collector.accept(new SingletonExprConnector(
                aX,
                connectorOffset - (right.getHeight() - 8) / 2,
                w -> this.right = w,
                () -> this.right
        ));
        this.right.collectConnectors(font, collector);
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        switch (arg) {
            case "left" -> this.left = obj;
            case "right" -> this.right = obj;
            default -> throw new IllegalArgumentException("unknown arg type for binary: " + arg);
        }
    }

    @Override
    public CodeWidget getByName(String arg) {
        return switch (arg) {
            case "left" -> this.left;
            case "right" -> this.right;
            default -> throw new IllegalArgumentException("unknown arg type for binary: " + arg);
        };
    }
}
