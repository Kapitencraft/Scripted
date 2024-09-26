package net.kapitencraft.scripted.lang.compile.parser;

import net.kapitencraft.scripted.lang.compile.Compiler;
import net.kapitencraft.scripted.lang.compile.VarTypeParser;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.holder.token.TokenTypeCategory;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;

@SuppressWarnings("ThrowableNotThrown")
public class AbstractParser {
    private final Map<TokenTypeCategory, TokenType[]> categoryLookup = createCategoryLookup();

    private static Map<TokenTypeCategory, TokenType[]> createCategoryLookup() {
        Map<TokenTypeCategory, TokenType[]> lookup = new HashMap<>();
        for (TokenTypeCategory category : TokenTypeCategory.values()) {
            lookup.put(category, Arrays.stream(values()).filter(tokenType -> tokenType.isCategory(category)).toArray(TokenType[]::new));
        }
        return lookup;
    }

    protected int current;
    protected Token[] tokens;
    protected VarTypeParser parser;
    protected final Compiler.ErrorLogger errorLogger;

    public AbstractParser(Compiler.ErrorLogger errorLogger) {
        this.errorLogger = errorLogger;
    }

    public void apply(Token[] toParse, VarTypeParser targetAnalyser) {
        this.current = 0;
        this.tokens = toParse;
        this.parser = targetAnalyser;
    }

    protected boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    protected boolean match(TokenTypeCategory category) {
        return match(categoryLookup.get(category));
    }

    protected boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    protected boolean isAtEnd() {
        return current == tokens.length - 1;
    }

    protected Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    protected Token peek() {
        return tokens[current];
    }

    protected Token previous() {
        return tokens[current - 1];
    }


    protected Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    protected LoxClass consumeVarType() {
        Token token = consume(IDENTIFIER, "<identifier> expected");
        LoxClass loxClass = parser.getClass(token.lexeme());
        if (loxClass == null) error(token, "unknown symbol");
        return loxClass;
    }

    protected Token consumeIdentifier() {
        return consume(IDENTIFIER, "<identifier> expected");
    }

    protected Token consumeBracketOpen(String method) {
        return this.consume(BRACKET_O, "Expected '(' after '" + method + "'.");
    }

    protected Token consumeCurlyOpen(String method) {
        return this.consume(C_BRACKET_O, "Expected '{' after '" + method + "'.");
    }

    protected Token consumeCurlyClose(String method) {
        return this.consume(C_BRACKET_C, "Expected '}' after " + method + ".");
    }

    protected Token consumeBracketClose(String method) {
        return this.consume(BRACKET_C, "Expected ')' after " + method + ".");
    }

    protected Token consumeEndOfArg() {
        return this.consume(EOA, "';' expected");
    }

    protected static class ParseError extends RuntimeException {}

    protected ParseError error(Token token, String message) {
        errorLogger.error(token, message);
        return new ParseError();
    }

    protected void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type() == EOA) return;

            switch (peek().type()) {
                case CLASS:
                case FUNC:
                case FOR:
                case IF:
                case WHILE:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
