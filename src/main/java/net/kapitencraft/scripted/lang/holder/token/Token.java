package net.kapitencraft.scripted.lang.holder.token;

import net.kapitencraft.scripted.lang.holder.LiteralHolder;

public record Token(TokenType type, String lexeme, LiteralHolder literal, int line, int lineStartIndex) {

    public String toString() {
        return String.format("Token{type=%s, lexeme=%s, literal=%s}@line%s", type, lexeme, literal, line);
    }
}
