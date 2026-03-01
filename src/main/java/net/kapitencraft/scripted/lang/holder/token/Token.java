package net.kapitencraft.scripted.lang.holder.token;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import net.kapitencraft.scripted.lang.run.VarTypeManager;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Token(TokenType type, String lexeme, LiteralHolder literal, int line, int lineStartIndex) {

    public @NotNull String toString() {
        return String.format("Token{type=%s, lexeme=\"%s\", literal=%s}@line%s", type, lexeme, literal, line);
    }

    public static Token readFromSubObject(JsonObject object, String name) {
        return fromJson(GsonHelper.getAsJsonObject(object, name));
    }

    public static Token fromJson(JsonObject object) {
        TokenType type = TokenType.valueOf(GsonHelper.getAsString(object, "type"));
        String lexeme = GsonHelper.getAsString(object, "lexeme");
        LiteralHolder literal = LiteralHolder.fromJson(GsonHelper.getAsJsonObject(object, "literal"));
        int line = GsonHelper.getAsInt(object, "line");
        int lineStartIndex = GsonHelper.getAsInt(object, "lineStartIndex");
        return new Token(type, lexeme, literal, line, lineStartIndex);
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
