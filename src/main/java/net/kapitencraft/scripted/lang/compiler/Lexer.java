package net.kapitencraft.scripted.lang.compiler;

import net.kapitencraft.scripted.lang.holder.token.Token;

import java.util.List;

public interface Lexer {
    List<Token> scanTokens();
}