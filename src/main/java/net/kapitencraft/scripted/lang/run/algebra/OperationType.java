package net.kapitencraft.scripted.lang.run.algebra;

import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.holder.token.TokenTypeCategory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum OperationType {
    ADDITION("add", TokenType.ADD, TokenType.ADD_ASSIGN),
    SUBTRACTION("sub", TokenType.SUB, TokenType.SUB_ASSIGN),
    MULTIPLICATION("mul", TokenType.MUL, TokenType.MUL_ASSIGN),
    DIVISION("div", TokenType.DIV, TokenType.DIV_ASSIGN),
    MODULUS("mod", TokenType.MOD, TokenType.MOD_ASSIGN),
    POTENCY("pow", TokenType.POW, TokenType.POW_ASSIGN),
    LEQUAL(null, TokenType.LEQUAL),
    NEQUAL(null, TokenType.NEQUAL),
    GEQUAL(null, TokenType.GEQUAL),
    LESS(null, TokenType.LESSER), // =
    MORE(null, TokenType.GREATER),
    EQUAL(null, TokenType.EQUAL);

    private final @Nullable String methodName;
    private final List<TokenType> type;


    OperationType(@Nullable String methodName, TokenType... type) {
        this.methodName = methodName;
        this.type = List.of(type);
    }

    /**
     * @return the name of the methods associated with this operation
     */
    public @Nullable String getMethodName() {
        return methodName;
    }

    public List<TokenType> getType() {
        return type;
    }

    public static OperationType of(TokenType operator) {
        for (OperationType type : values()) {
            if (type.type.contains(operator)) return type;
        }
        return null;
    }

    public boolean is(TokenTypeCategory category) {
        return this.type.stream().anyMatch(t -> t.isCategory(category));
    }

    public boolean isComparator() {
        return is(TokenTypeCategory.COMPARATORS) || is(TokenTypeCategory.EQUALITY);
    }
}
