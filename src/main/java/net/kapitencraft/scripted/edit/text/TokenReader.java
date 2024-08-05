package net.kapitencraft.scripted.edit.text;

import net.kapitencraft.scripted.edit.Token;
import net.minecraft.util.Mth;

import java.util.List;

public class TokenReader {
    private final List<Token> tokens;
    private int cursor = 0;

    public TokenReader(List<Token> tokens) {
        this.tokens = tokens;
    }

    public int getCursor() {
        return cursor;
    }

    public Token getCurrent() {
        return tokens.get(cursor);
    }

    public void setCursor(int cursor) {
        this.cursor = Mth.clamp(cursor, 0, tokens.size() - 1);
    }

    public void next() {
        this.setCursor(this.cursor + 1);
    }
}
