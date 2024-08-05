package net.kapitencraft.scripted.edit.text.builder;

import net.kapitencraft.scripted.edit.Token;
import net.kapitencraft.scripted.edit.text.node.CompilerNode;
import net.kapitencraft.scripted.edit.text.node.TokenNode;
import org.jetbrains.annotations.Nullable;

public class TokenCompilerBuilder<S> extends CompilerBuilder<S, TokenCompilerBuilder<S>> {
    private final Token.Type type;
    private final @Nullable String value;

    public TokenCompilerBuilder(Token.Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public static <T> TokenCompilerBuilder<T> token(Token.Type type) {
        return new TokenCompilerBuilder<>(type, null);
    }

    public static <T> TokenCompilerBuilder<T> token(Token.Type type, String value) {
        return new TokenCompilerBuilder<>(type, value);
    }

    @Override
    protected TokenCompilerBuilder<S> getThis() {
        return this;
    }

    @Override
    public CompilerNode<S> build() {
        return new TokenNode<>(getRedirect(), getRedirectModifier(), isFork(), this.type, this.value);
    }
}
