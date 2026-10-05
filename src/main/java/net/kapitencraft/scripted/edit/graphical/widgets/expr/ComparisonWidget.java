package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.MapCodec;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class ComparisonWidget extends AbstractBinaryOperationWidget<ComparisonWidget.Operation> {
    public static final MapCodec<ComparisonWidget> CODEC = codec(
            Operation.CODEC,
            ParamWidget.NUM,
            Operation.EQUAL,
            ComparisonWidget::new
    );

    private ComparisonWidget(ExprCodeWidget left, Operation operation, ExprCodeWidget right) {
        super(left, Operation.values(), operation, right, CodeWidgetSprites.BOOL_EXPR);
    }

    public ComparisonWidget() {
        this(ParamWidget.NUM.get(), Operation.EQUAL, ParamWidget.NUM.get());
    }

    @Override
    public @NotNull Type getType() {
        return Type.COMPARISON;
    }

    @Override
    public ExprCodeWidget copy() {
        return new ComparisonWidget(
                this.left,
                this.operatorWidget.getValue(),
                this.right
        );
    }

    enum Operation implements StringRepresentable {
        LESS("<"),
        LESS_OR_EQUAL("<="),
        EQUAL("=="),
        GREATER(">"),
        GREATER_OR_EQUAL(">="),
        UNEQUAL("!=");

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
