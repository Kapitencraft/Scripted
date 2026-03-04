package net.kapitencraft.scripted.lang.holder.token;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Token(TokenType type, String lexeme, LiteralHolder literal, int line, int lineStartIndex) {

    public @NotNull String toString() {
        return String.format("Token{type=%s, lexeme=\"%s\", literal=%s}@line%s", type, lexeme, literal, line);
    }

    public static Token createNative(String lexeme) {
        return new Token(TokenType.IDENTIFIER, lexeme, LiteralHolder.EMPTY, -1, -1);
    }

    public Token after() {
        return new Token(this.type, this.lexeme, this.literal, this.line, this.lineStartIndex + this.lexeme.length());
    }

    public Token withPrefix(@Nullable String namePrefix) {
        return new Token(this.type, namePrefix + this.lexeme, this.literal, this.line, this.lineStartIndex);
    }

    public Token asIdentifier(String newLexeme) {
        return new Token(TokenType.IDENTIFIER, newLexeme, LiteralHolder.EMPTY, this.line, this.lineStartIndex);
    }

    public Token lexemeAsLiteral() {
        return new Token(TokenType.STR, this.lexeme, new LiteralHolder(this.lexeme, VarTypeManager.STRING.get()), this.line, this.lineStartIndex);
    }
}
