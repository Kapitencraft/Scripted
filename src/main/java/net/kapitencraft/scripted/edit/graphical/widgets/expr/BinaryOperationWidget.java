package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.fetch.ExprWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.kapitencraft.scripted.edit.graphical.inserter.expr.ArgumentInserter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

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

    public BinaryOperationWidget() {}

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
    public GhostInserter getGhostWidgetTarget(int x, int y, Font font, boolean isBlock) {
        if (x < this.getWidth(font))
            return ArgumentInserter.create(x, y, font, "§op", (s, widget) -> {
                if (s.equals("left"))
                    left = widget;
                else if (s.equals("right")) {
                    right = widget;
                }
                throw new IllegalArgumentException("unknown operation argument: " + s);
            }, Map.of("left", left, "right", right));
        return null;
    }

    @Override
    public void update(@Nullable MethodContext context) {
        this.left.update(context);
        this.right.update(context);
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return ExprWidgetFetchResult.fromExprList(4,  x, y, font, this, "§op", Map.of("left", left, "right", right));
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
