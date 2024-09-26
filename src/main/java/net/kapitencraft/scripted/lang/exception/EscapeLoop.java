package net.kapitencraft.scripted.lang.exception;

import net.kapitencraft.scripted.lang.holder.token.Token;

public class EscapeLoop extends RuntimeException {
    public final Token token;

    public EscapeLoop(Token token) {
        super(null, null, false, false);

        this.token = token;
    }
}
