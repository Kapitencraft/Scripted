package net.kapitencraft.scripted.edit.text;

import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.edit.Token;

import java.util.List;

public abstract class Tokenizer {

    public abstract List<Token> tokenize(String content);

    protected static Token last(List<Token> tokens) {
        return tokens.get(tokens.size() - 1);
    }

    protected static void removeLast(List<Token> tokens) {
        tokens.remove(tokens.size() - 1);
    }

    protected static Token readNum(int start, String content) {
        char c;
        int i = start;
        do {
            i++;
            c = content.charAt(i);
        } while (Character.isDigit(c) || c == '.');
        return new Token(content.substring(start, i), Token.Type.PRIM_NUM);
    }
}
