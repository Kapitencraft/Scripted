package net.kapitencraft.scripted.edit.graphical.widgets.expr.binary;

import com.mojang.serialization.MapCodec;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ParamWidget;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class BinaryNumOperationWidget extends AbstractBinaryOperationWidget<BinaryNumOperationWidget.Operation> {
    public static final MapCodec<BinaryNumOperationWidget> CODEC = codec(
            Operation.CODEC,
            ParamWidget.NUM,
            Operation.ADD,
            BinaryNumOperationWidget::new
    );

    private BinaryNumOperationWidget(ExprCodeWidget left, Operation operation, ExprCodeWidget right) {
        super(left, Operation.values(), operation, right);
    }

    public BinaryNumOperationWidget() {
        this(ParamWidget.NUM, Operation.ADD, ParamWidget.NUM);
    }

    @Override
    public @NotNull Type getType() {
        return Type.ALGEBRA;
    }

    @Override
    public ExprCodeWidget copy() {
        return new BinaryNumOperationWidget(
                this.left,
                this.operatorWidget.getValue(),
                this.right
        );
    }

    @Override
    public void insertById(int arg, @NotNull ExprCodeWidget obj) {

    }

    @Override
    public CodeWidget getByIndex(int argName) {
        return null;
    }

    @Override
    public Expr parse() {
        return null;
    }

    enum Operation implements StringRepresentable {
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
