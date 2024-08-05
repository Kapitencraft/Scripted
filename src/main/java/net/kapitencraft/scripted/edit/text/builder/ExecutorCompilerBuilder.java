package net.kapitencraft.scripted.edit.text.builder;

import net.kapitencraft.scripted.edit.text.node.CompilerNode;
import net.kapitencraft.scripted.edit.text.node.ExecutorNode;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ExecutorCompilerBuilder<S, T> extends CompilerBuilder<S, ExecutorCompilerBuilder<S, T>> {
    private final HashMap<CompilerNode<S>, BiConsumer<CompilerContextBuilder<S>, T>> applier = new HashMap<>();
    private final Function<CompilerContextBuilder<S>, T> creator;
    private boolean loop;

    public ExecutorCompilerBuilder(Function<CompilerContextBuilder<S>, T> creator) {
        this.creator = creator;
    }

    @Override
    protected ExecutorCompilerBuilder<S, T> getThis() {
        return this;
    }

    public ExecutorCompilerBuilder<S, T> extension(CompilerNode<S> node, BiConsumer<CompilerContextBuilder<S>, T> consumer) {
        applier.put(node, consumer);
        return this;
    }

    public ExecutorCompilerBuilder<S, T> loops() {
        loop = true;
        return this;
    }

    @Override
    public CompilerNode<S> build() {
        return new ExecutorNode<>(getRedirect(), getRedirectModifier(), isFork(), this.creator, this.applier);
    }
}