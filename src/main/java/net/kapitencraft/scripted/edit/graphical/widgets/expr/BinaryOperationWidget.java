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
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class BinaryOperationWidget implements ExprCodeWidget {
    public static final MapCodec<BinaryOperationWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExprCodeWidget.CODEC.optionalFieldOf("left", ParamWidget.NUM).forGetter(w -> w.left),
            Operation.CODEC.optionalFieldOf("operation", Operation.ADD).forGetter(w -> w.operatorWidget.getValue()),
            ExprCodeWidget.CODEC.optionalFieldOf("right", ParamWidget.NUM).forGetter(w -> w.right)
    ).apply(i, BinaryOperationWidget::new));

    private ExprCodeWidget left = ParamWidget.NUM;
    private Operation operation;
    private final ListSelectionWidget<Operation> operatorWidget = new ListSelectionWidget<>(List.of(Operation.values()), Operation::getSerializedName);
    private ExprCodeWidget right = ParamWidget.NUM;

    private BinaryOperationWidget(ExprCodeWidget left, Operation operation, ExprCodeWidget right) {
        this.left = left;
        this.operation = operation;
        this.right = right;
    }

    public BinaryOperationWidget(ExprCodeWidget left, TokenType operation, ExprCodeWidget right) {
        this(left, Operation.of(operation), right);
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
        return 6 + RenderHelper.getVisualTextWidth(font, "§op", List.of(left, operatorWidget, right));
    }

    @Override
    public int getHeight() {
        return Math.max(18, ExprCodeWidget.getHeightFromEntries(List.of(left, right)));
    }

    @Override
    public ExprCodeWidget copy() {
        return new BinaryOperationWidget(
                left, operation, right
        );
    }

    @Override
    public Expr parse() {
        return new Expr.Binary(
                this.left.parse(),
                this.right.parse(),
                operation.asToken(),
                null,
                null
        );
    }

    @Override
    public void update(@Nullable MethodContext context) {
        this.left.update(context);
        this.right.update(context);
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return ExprWidgetFetchResult.fromExprList(4, x, y, font, this, "§op", List.of(left, operatorWidget, right));
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        this.left.registerInteractions(xOrigin, yOrigin, font, sink);
        this.operatorWidget.registerInteractions(xOrigin + RenderHelper.getPartialWidth(font, "§op", List.of(left, operatorWidget, right), 1), yOrigin, font, sink);
        this.right.registerInteractions(xOrigin, yOrigin, font, sink);
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        List<ExprCodeWidget> params = List.of(left, operatorWidget, right);
        RenderHelper.forPartialWidth(font, "§op", params, (argIndex, integer) -> {
            if (argIndex != 1) {
                collector.accept(new ArgumentExprConnector(aX + integer, aY, this, argIndex));
                params.get(argIndex).collectConnectors(aX + integer, aY, font, collector);
            }
        });
    }

    @Override
    public void insertById(int arg, @NotNull ExprCodeWidget obj) {
        switch (arg) {
            case 0 -> this.left = obj;
            case 2 -> this.right = obj;
            default -> throw new IllegalArgumentException("unknown arg type for binary: " + arg);
        }
    }

    @Override
    public CodeWidget getByIndex(int argName) {
        return switch (argName) {
            case 0 -> this.left;
            case 2 -> this.right;
            default -> throw new IllegalArgumentException("unknown arg type for binary: " + argName);
        };
    }

    private enum Operation implements StringRepresentable {
        ADD("+", TokenType.ADD),
        SUB("-", TokenType.SUB),
        MUL("*", TokenType.MUL),
        DIV("/", TokenType.DIV),
        MOD("%", TokenType.MOD),
        POW("**", TokenType.POW),
        AND("&&", TokenType.AND),
        OR("||", TokenType.OR),
        XOR("^", TokenType.XOR);

        public static final EnumCodec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values);

        private final String literal;
        private final TokenType type;

        Operation(String literal, TokenType type) {
            this.literal = literal;
            this.type = type;
        }

        public static Operation of(TokenType type) {
            return switch (type) {
                case ADD -> ADD;
                case SUB -> SUB;
                case MUL -> MUL;
                case DIV -> DIV;
                case MOD -> MOD;
                case POW -> POW;
                case AND -> AND;
                case OR -> OR;
                case XOR -> XOR;
                default -> throw new IllegalArgumentException("not an operation: " + type);
            };
        }

        @Override
        public @NotNull String getSerializedName() {
            return literal;
        }

        public Token asToken() {
            return new Token(
                    this.type,
                    this.literal,
                    LiteralHolder.EMPTY,
                    -1,
                    -1
            );
        }
    }
}
