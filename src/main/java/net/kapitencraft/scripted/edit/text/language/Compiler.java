package net.kapitencraft.scripted.edit.text.language;

import net.kapitencraft.scripted.code.oop.Script;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.edit.Token;
import net.kapitencraft.scripted.edit.text.builder.ArgumentCompilerBuilder;
import net.kapitencraft.scripted.edit.text.builder.TokenCompilerBuilder;

import java.util.List;
import java.util.function.Function;

public abstract class Compiler {
    protected final List<Token> tokens;

    protected Compiler(List<Token> tokens) {
        this.tokens = tokens;
    }

    public abstract Script castScript();

    public static <S> TokenCompilerBuilder<S> token(Token.Type type) {
        return TokenCompilerBuilder.token(type);
    }

    public static <S> TokenCompilerBuilder<S> token(Token.Type type, String value) {
        return TokenCompilerBuilder.token(type, value);
    }

    public static <S> ArgumentCompilerBuilder<S, VarType<?>> varType() {
        return new ArgumentCompilerBuilder<>(VarType::read, Token.Type.VAR_TYPE);
    }

    public static <S> ArgumentCompilerBuilder<S, String> varName() {
        return new ArgumentCompilerBuilder<>(Function.identity(), Token.Type.VAR_NAME);
    }
}