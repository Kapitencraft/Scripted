package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
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

public class BinaryOperationWidget implements ExprCodeWidget {
    public static final MapCodec<BinaryOperationWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExprCodeWidget.CODEC.optionalFieldOf("left", ParamWidget.NUM).forGetter(w -> w.left),
            Operation.CODEC.optionalFieldOf("operation", Operation.ADD).forGetter(w -> w.operatorWidget.getValue()),
            ExprCodeWidget.CODEC.optionalFieldOf("right", ParamWidget.NUM).forGetter(w -> w.right)
    ).apply(i, BinaryOperationWidget::new));

    private ExprCodeWidget left = ParamWidget.NUM;
    private final ListSelectionWidget<Operation> operatorWidget = new ListSelectionWidget<>(List.of(Operation.values()), Operation::getSerializedName);
    private ExprCodeWidget right = ParamWidget.NUM;

    private BinaryOperationWidget(ExprCodeWidget left, Operation operation, ExprCodeWidget right) {
        this.left = left;
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

    }

    @Override
    public int getWidth(Font font) {
        return 6 + RenderHelper.getVisualTextWidth(font, "§op", Map.of("left", left, "right", right));
    }

    @Override
    public int getHeight() {
        return Math.max(18, ExprCodeWidget.getHeightFromEntries(List.of(left, right)));
    }

    @Override
    public ExprCodeWidget copy() {
        return null;
    }

    @Override
    public void update(@Nullable MethodContext context) {
        this.left.update(context);
        this.right.update(context);
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return ExprWidgetFetchResult.fromExprList(4, x, y, font, this, "§op", Map.of("left", left, "op", operatorWidget, "right", right));
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        this.left.registerInteractions(xOrigin, yOrigin, font, sink);
        this.operatorWidget.registerInteractions(xOrigin + RenderHelper.getPartialWidth(font, "§op", Map.of("left", left, "op", operatorWidget, "right", right), "op"), yOrigin, font, sink);
        this.right.registerInteractions(xOrigin, yOrigin, font, sink);
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        Map<String, ExprCodeWidget> params = Map.of("left", left, "op", operatorWidget, "right", right);
        RenderHelper.forPartialWidth(font, "§op", params, (s, integer) -> {
            if (!"op".equals(s)) {
                collector.accept(new ArgumentExprConnector(aX + integer, aY, this, s));
                params.get(s).collectConnectors(aX + integer, aY, font, collector);
            }
        });
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
    public CodeWidget getByName(String argName) {
        return switch (argName) {
            case "left" -> this.left;
            case "right" -> this.right;
            default -> throw new IllegalArgumentException("unknown arg type for binary: " + argName);
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
