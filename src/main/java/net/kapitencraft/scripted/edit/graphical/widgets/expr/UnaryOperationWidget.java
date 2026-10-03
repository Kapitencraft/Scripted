package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.TextRenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.ArgumentExprConnector;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
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

public class UnaryOperationWidget implements ExprCodeWidget {
    public static final MapCodec<UnaryOperationWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Operation.CODEC.optionalFieldOf("operation", Operation.SQRT).forGetter(w -> w.operatorWidget.getValue()),
            ExprCodeWidget.CODEC.optionalFieldOf("right", ParamWidget.NUM).forGetter(w -> w.right)
    ).apply(i, UnaryOperationWidget::new));

    private final ListSelectionWidget<Operation> operatorWidget = new ListSelectionWidget<>(List.of(Operation.values()), Operation::getSerializedName);
    private ExprCodeWidget right = ParamWidget.NUM;

    private UnaryOperationWidget(Operation operation, ExprCodeWidget right) {
        this.operatorWidget.set(operation);
        this.right = right;
    }

    public UnaryOperationWidget() {
    }

    @Override
    public @NotNull Type getType() {
        return Type.UNARY;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(CodeWidgetSprites.NUMBER_EXPR, renderX, renderY, getWidth(font), getHeight());
        TextRenderHelper.renderVisualText(graphics, font, renderX, renderY + 6 + (getHeight() - 20) / 2, "§un_op", Map.of("op", this.operatorWidget, "right", right));
    }

    @Override
    public int getWidth(Font font) {
        return 6 + TextRenderHelper.getVisualTextWidth(font, "§un_op", Map.of("op", this.operatorWidget, "right", right));
    }

    @Override
    public int getHeight() {
        return Math.max(18, right.getHeight() + 4);
    }

    @Override
    public ExprCodeWidget copy() {
        return new UnaryOperationWidget(
                this.operatorWidget.getValue(), this.right
        );
    }

    @Override
    public void update(@Nullable MethodContext context, Font font) {
        this.right.update(context, font);
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return ExprWidgetFetchResult.fromExprList(4, x, y, font, this, "§un_op", Map.of("op", operatorWidget, "right", right));
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        this.operatorWidget.registerInteractions(xOrigin + TextRenderHelper.getPartialWidth(font, "§un_op", Map.of("op", operatorWidget, "right", right), "op"), yOrigin, font, sink);
        this.right.registerInteractions(xOrigin, yOrigin, font, sink);
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        int width = TextRenderHelper.getPartialWidth(font, "§un_op", Map.of("op", operatorWidget, "right", right), "right");
        int connectorOffset = aY + (getHeight() - 20) / 2;
        collector.accept(new ArgumentExprConnector(aX + 4 + width, aY + connectorOffset, this, "right"));
        this.right.collectConnectors(aX +  4 + width, aY, font, collector);
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        if (arg.equals("right")) {
            this.right = obj;
        } else {
            throw new IllegalArgumentException("unknown arg type for binary: " + arg);
        }
    }

    @Override
    public CodeWidget getByName(String arg) {
        return switch (arg) {
            case "right" -> this.right;
            default -> throw new IllegalArgumentException("unknown arg type for unary: " + arg);
        };
    }

    private enum Operation implements StringRepresentable {
        SQRT("sqrt"),
        ABS("abs"),
        NOT("not"),
        NEGATIVE("-");

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
