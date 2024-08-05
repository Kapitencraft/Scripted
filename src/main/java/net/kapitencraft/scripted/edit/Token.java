package net.kapitencraft.scripted.edit;

public class Token {
    public final String value;
    public final Type type;

    public Token(String value, Type type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
        return type + "{" + value + "}";
    }

    public enum Type {//seems like a lot, and yes it is
        PRIM_STRING, PRIM_NUM, PRIM_REG_ELEMENT, PRIM_CHAR,
        WHEN_CONDITION_SEPARATOR, WHEN_FALSE_SEPARATOR,
        ASSIGN, ASSIGN_WITH_OPERATION,
        MODIFIER, VAR_TYPE, METHOD_NAME, VAR_NAME,
        FOR_IDENTIFIER, IF_IDENTIFIER, ELSE_IDENTIFIER, WHILE_IDENTIFIER, DO_IDENTIFIER,
        SEPARATOR,
        EXPR_END,
        NEXT_PARAM,
        NEW_LINE, EOF,
        BRACKET_OPEN, BRACKET_CLOSE,
        CURLY_BRACKET_OPEN, CURLY_BRACKET_CLOSE,
        ADD, MULT, SUB, DIV, MOD,
        AND, OR, XOR, NOT,
        EQUAL, GREATER, LESSER;
    }
}