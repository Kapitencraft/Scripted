package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.TextRenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.connector.SingletonExprConnector;
import net.kapitencraft.scripted.edit.graphical.fetch.ExprWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BinaryOperationWidget implements ExprCodeWidget {
    public static final MapCodec<BinaryOperationWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExprCodeWidget.CODEC.optionalFieldOf("left", ParamWidget.NUM).forGetter(w -> w.left),
            Operation.CODEC.optionalFieldOf("operation", Operation.ADD).forGetter(w -> w.operatorWidget.getValue()),
            ExprCodeWidget.CODEC.optionalFieldOf("right", ParamWidget.NUM).forGetter(w -> w.right)
    ).apply(i, BinaryOperationWidget::new));

    private ExprCodeWidget left = ParamWidget.NUM;
    private final EnumSelectionWidget<Operation> operatorWidget = new EnumSelectionWidget<>(List.of(Operation.values()), Operation::getSerializedName);
    private ExprCodeWidget right = ParamWidget.NUM;

    private BinaryOperationWidget(ExprCodeWidget left, Operation operation, ExprCodeWidget right) {
        this.left = left;
        this.operatorWidget.set(operation);
        this.right = right;
    }

    public BinaryOperationWidget() {
    }

    @Override
    public @NotNull Type getType() {
        return Type.BINARY;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(CodeWidgetSprites.NUMBER_EXPR, renderX, renderY, getWidth(font), getHeight());
        TextRenderHelper.renderVisualText(graphics, font, renderX, renderY + 6 + (getHeight() - 20) / 2, "§bin_op", Map.of("left", left, "op", this.operatorWidget, "right", right));
    }

    @Override
    public int getWidth(Font font) {
        return 6 + TextRenderHelper.getVisualTextWidth(font, "§bin_op", Map.of("left", left, "op", this.operatorWidget, "right", right));
    }

    @Override
    public int getHeight() {
        return Math.max(18, ExprCodeWidget.getHeightFromEntries(List.of(left, right)) + 4);
    }

    @Override
    public ExprCodeWidget copy() {
        return new BinaryOperationWidget(
                this.left, this.operatorWidget.getValue(), this.right
        );
    }

    @Override
    public void update(@Nullable MethodContext context, Font font) {
        this.left.update(context, font);
        this.right.update(context, font);
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
                left = ParamWidget.NUM;
            return result.setRemoved();
        }
        x -= leftWidth + spaceWidth + this.operatorWidget.getWidth(font);
        int rightWidth = this.right.getWidth(font);
        if (x < rightWidth) {
            WidgetFetchResult result = this.right.fetchAndRemoveHovered(x, y, font);
            if (result == null)
                return ExprWidgetFetchResult.notRemoved(this, oX, y);
            if (!result.removed())
                right = ParamWidget.NUM;
            return result.setRemoved();
        }
        return ExprWidgetFetchResult.notRemoved(this, oX, y);
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        this.left.registerInteractions(xOrigin, yOrigin, font, sink);
        int spaceWidth = font.width(" ");
        xOrigin += this.left.getWidth(font) + spaceWidth;
        this.operatorWidget.registerInteractions(xOrigin, yOrigin, font, sink);
        xOrigin += this.operatorWidget.getWidth(font) + spaceWidth;
        this.right.registerInteractions(xOrigin, yOrigin, font, sink);
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        int spaceWidth = font.width(" ");
        int connectorOffset = aY + 6 + (getHeight() - 20) / 2;
        aX += 4;
        collector.accept(new SingletonExprConnector(
                aX,
                connectorOffset - (left.getHeight() - 8) / 2,
                w -> this.left = w,
                () -> this.left
        ));
        this.left.collectConnectors(aX, aY, font, collector);
        aX += this.left.getWidth(font) + 2 * spaceWidth + this.operatorWidget.getWidth(font);
        collector.accept(new SingletonExprConnector(
                aX,
                connectorOffset - (right.getHeight() - 8) / 2,
                w -> this.right = w,
                () -> this.right
        ));
        this.right.collectConnectors(aX, aY, font, collector);
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

    private enum Operation implements StringRepresentable {
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        MOD("%"),
        POW("**");

        public static final EnumCodec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values);

        private final String literal;

        Operation(String literal) {
            this.literal = literal;
        }

        @Override
        public @NotNull String getSerializedName() {
            return literal;
        }
    }
}
