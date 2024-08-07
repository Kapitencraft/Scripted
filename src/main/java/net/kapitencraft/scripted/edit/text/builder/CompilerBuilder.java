package net.kapitencraft.scripted.edit.text.builder;

import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.SingleRedirectModifier;
import net.kapitencraft.scripted.edit.text.node.CompilerNode;
import net.kapitencraft.scripted.edit.text.node.RootNode;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

//TODO load code dynamically
public abstract class CompilerBuilder<S, T extends CompilerBuilder<S, T>> {
    private final RootNode<S> arguments = new RootNode<>();
    private CompilerNode<S> target;
    private RedirectModifier<S> modifier = null;
    private boolean forks;

    protected abstract T getThis();

    public <J extends CompilerBuilder<S, ?>> J then(final J argument) {
        if (target != null) {
            throw new IllegalStateException("Cannot add children to a redirected node");
        }
        arguments.addChild(argument.build());
        return argument;
    }

    public Collection<CompilerNode<S>> getArguments() {
        return arguments.getChildren();
    }

    public <K> ExecutorCompilerBuilder<S, K> executes(Function<CompilerContextBuilder<S>, K> creator) {
        return new ExecutorCompilerBuilder<>(creator);
    }

    public T redirect(final CompilerNode<S> target) {
        return forward(target, null, false);
    }

    public T redirect(final CompilerNode<S> target, final SingleRedirectModifier<S> modifier) {
        return forward(target, modifier == null ? null : o -> Collections.singleton(modifier.apply(o)), false);
    }

    public T fork(final CompilerNode<S> target, final RedirectModifier<S> modifier) {
        return forward(target, modifier, true);
    }

    public T forward(final CompilerNode<S> target, final RedirectModifier<S> modifier, final boolean fork) {
        if (!arguments.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot forward a node with children");
        }
        this.target = target;
        this.modifier = modifier;
        this.forks = fork;
        return getThis();
    }

    public CompilerNode<S> getRedirect() {
        return target;
    }

    public RedirectModifier<S> getRedirectModifier() {
        return modifier;
    }

    public boolean isFork() {
        return forks;
    }

    public abstract CompilerNode<S> build();
}
