package net.kapitencraft.scripted.edit.text.builder;

import net.kapitencraft.scripted.edit.Token;
import net.kapitencraft.scripted.edit.text.node.ArgumentNode;
import net.kapitencraft.scripted.edit.text.node.CompilerNode;

import java.util.function.Function;

public class ArgumentCompilerBuilder<S, T> extends CompilerBuilder<S, ArgumentCompilerBuilder<S, T>> {
    private final Function<String, T> mapper;
    private final Token.Type type;

    public ArgumentCompilerBuilder(Function<String, T> mapper, Token.Type type) {
        this.mapper = mapper;
        this.type = type;
    }


    @Override
    protected ArgumentCompilerBuilder<S, T> getThis() {
        return this;
    }

    @Override
    public CompilerNode<S> build() {
        return new ArgumentNode<>(getRedirect(), getRedirectModifier(), isFork(), mapper, type);
    }
}
