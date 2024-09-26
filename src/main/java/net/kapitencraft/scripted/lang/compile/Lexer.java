package net.kapitencraft.scripted.lang.compile;

import net.kapitencraft.scripted.lang.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;
import net.kapitencraft.scripted.lang.run.Main;

import java.util.ArrayList;
import java.util.List;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;

public class Lexer {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private final String[] lines;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int indexAtLineStart = 0;

    private void nextLine() {
        line++;
        indexAtLineStart = current;
    }

    public Lexer(String source) {
        this.source = source;
        this.lines = source.split("\n", Integer.MAX_VALUE);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        if (type == TRUE) addToken(type, true, VarTypeManager.BOOLEAN);
        else if (type == FALSE) addToken(type, false, VarTypeManager.BOOLEAN);
        else addToken(type, null, null);
    }

    private void addToken(TokenType type, Object literal, LoxClass literalClass) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, new LiteralHolder(literal, literalClass), line, start - indexAtLineStart));
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line, current - indexAtLineStart + 1));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(BRACKET_O); break;
            case ')': addToken(BRACKET_C); break;
            case '{': addToken(C_BRACKET_O); break;
            case '}': addToken(C_BRACKET_C); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case ';': addToken(EOA); break;
            case '-':
                addToken(match('>') ? LAMBDA : match('-') ? SHRINK : match('=') ? SUB_ASSIGN : SUB);
                break;
            case '+':
                addToken(match('+') ? GROW : match('=') ? ADD_ASSIGN : ADD);
                break;
            case '*':
                addToken(match('*') ? POW : match('=') ? MUL_ASSIGN : MUL);
                break;
            case '%':
                addToken(match('=') ? MOD_ASSIGN : MOD);
                break;
            case '!':
                addToken(match('=') ? NEQUAL : NOT);
                break;
            case '=':
                addToken(match('=') ? EQUAL : ASSIGN);
                break;
            case '<':
                addToken(match('=') ? LEQUAL : LESSER);
                break;
            case '>':
                addToken(match('=') ? GEQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(match('=') ? DIV_ASSIGN : DIV);
                }
                break;
            case '&':
                if (match('&'))
                    addToken(AND);
                else if (match('='))
                    addToken(AND_ASSIGN);
                else error("unexpected token");
            case '|':
                if (match('|'))
                    addToken(OR);
                else if (match('='))
                    addToken(OR_ASSIGN);
                else error("unexpected token");
            case '^':
                if (match('='))
                    addToken(XOR_ASSIGN);
                else
                    addToken(XOR);
            case ' ':
            case '\t':
                //addToken(IN_LINE);
            case '\r':
                // Ignore whitespace.
                break;
            case '\n':
                nextLine();
                break;
            case '"': string(); break;
            case ':':
                addToken(WHEN_FALSE);
                break;
            case '?':
                addToken(WHEN_CONDITION);
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error("Unexpected character");
                }
        }
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private String getCurrentLine() {
        return this.lines[line - 1];
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        boolean seenDecimal = match('.');
        current--; //jump back
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (!seenDecimal && peek() == '.' && isDigit(peekNext())) {
            seenDecimal = true;
            do advance();
            while (isDigit(peek()));
        }
        String literal = source.substring(start, current);
        if (peek() == 'f' || peek() == 'F') { //float :hypers:
            advance();
            addToken(NUM, Float.parseFloat(literal), VarTypeManager.FLOAT);
        }

        if (seenDecimal) addToken(NUM, Double.parseDouble(literal), VarTypeManager.DOUBLE);
        else addToken(NUM, Integer.parseInt(literal), VarTypeManager.INTEGER);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        addToken(VarTypeManager.getType(text));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') nextLine();
            advance();
        }

        if (isAtEnd()) {
            error("Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STR, value, VarTypeManager.STRING);
    }

    private void error(String msg) {
        Main.error(line, msg, getCurrentLine());
    }

    public String[] getLines() {
        return this.lines;
    }
}