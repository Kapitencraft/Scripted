package net.kapitencraft.scripted.edit;

import net.minecraft.ChatFormatting;

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
        PRIM_STRING(Category.PRIM_CONSTRUCTOR), PRIM_NUM(Category.PRIM_CONSTRUCTOR), PRIM_REG_ELEMENT(Category.PRIM_CONSTRUCTOR), PRIM_CHAR(Category.PRIM_CONSTRUCTOR),
        WHEN_CONDITION_SEPARATOR(Category.OPERATION), WHEN_FALSE_SEPARATOR(Category.OPERATION),
        ASSIGN(Category.OPERATION), ASSIGN_WITH_OPERATION(Category.OPERATION),
        MODIFIER(Category.KEY_WORD), VAR_TYPE(Category.VAR_TYPE), METHOD_NAME(null), VAR_NAME(null),
        FOR_IDENTIFIER(Category.KEY_WORD), IF_IDENTIFIER(Category.KEY_WORD), ELSE_IDENTIFIER(Category.KEY_WORD), WHILE_IDENTIFIER(Category.KEY_WORD), DO_IDENTIFIER(Category.KEY_WORD),
        RETURN_IDENTIFIER(Category.KEY_WORD), BREAK_IDENTIFIER(Category.KEY_WORD), CONTINUE_IDENTIFIER(Category.KEY_WORD),
        SEPARATOR(null),
        EXPR_END(null),
        NEXT_PARAM(null),
        NEW_LINE(null), EOF(null),
        BRACKET_OPEN(null), BRACKET_CLOSE(null),
        CURLY_BRACKET_OPEN(null), CURLY_BRACKET_CLOSE(null),
        ADD(Category.OPERATION), MULT(Category.OPERATION), SUB(Category.OPERATION), DIV(Category.OPERATION), MOD(Category.OPERATION),
        AND(Category.OPERATION), OR(Category.OPERATION), XOR(Category.OPERATION), NOT(Category.OPERATION),
        EQUAL(Category.OPERATION), GREATER(Category.OPERATION), LESSER(Category.OPERATION), LEQUAL(Category.OPERATION), GEQUAL(Category.OPERATION), NEQUAL(Category.OPERATION);

        private final Category category;

        Type(Category category) {
            this.category = category;
        }

        public Category getCategory() {
            return category;
        }

        public enum Category {
            KEY_WORD("Key Word", "key_word", ChatFormatting.LIGHT_PURPLE),
            OPERATION("Operation", "operation", ChatFormatting.YELLOW),
            NAME("Name", "name", ChatFormatting.WHITE),
            VAR_TYPE("Var Type", "var_type", ChatFormatting.AQUA),
            PRIM_CONSTRUCTOR("Primitive Constructor", "prim_constructor", ChatFormatting.GREEN);

            private final String name;
            private final String id;
            private final ChatFormatting defaulted;

            Category(String name, String id, ChatFormatting defaulted) {
                this.name = name;
                this.id = id;
                this.defaulted = defaulted;
            }

            public String getName() {
                return name;
            }

            public String getId() {
                return id;
            }

            public ChatFormatting getDefault() {
                return defaulted;
            }
        }
    }
}