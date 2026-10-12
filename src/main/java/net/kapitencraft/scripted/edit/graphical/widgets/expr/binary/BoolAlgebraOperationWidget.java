package net.kapitencraft.scripted.edit.graphical.widgets.expr.binary;

import com.mojang.serialization.MapCodec;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ParamWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.UnaryOperationWidget;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class BoolAlgebraOperationWidget extends AbstractBinaryOperationWidget<BoolAlgebraOperationWidget.Operation> {
    public static final MapCodec<BoolAlgebraOperationWidget> CODEC = codec(
            Operation.CODEC,
            ParamWidget.CONDITION,
            Operation.AND,
            BoolAlgebraOperationWidget::new
    );

    private BoolAlgebraOperationWidget(ExprCodeWidget left, Operation operation, ExprCodeWidget right) {
        super(left, Operation.values(), operation, right);
    }

    public BoolAlgebraOperationWidget() {
        this(ParamWidget.CONDITION, Operation.AND, ParamWidget.CONDITION);
    }

    @Override
    public @NotNull Type getType() {
        return Type.BOOL_ALGEBRA;
    }

    @Override
    public ExprCodeWidget copy() {
        return new UnaryOperationWidget();
    }

    enum Operation implements StringRepresentable {
        AND("and"),
        OR("or"),
        XOR("xor");

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
