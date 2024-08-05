package net.kapitencraft.scripted.edit.text.node;

import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kapitencraft.scripted.edit.text.TokenReader;
import net.kapitencraft.scripted.edit.text.builder.CompilerContextBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class CompilerNode<S> {
    private final Map<String, CompilerNode<S>> children = new LinkedHashMap<>();
    private final Map<String, TokenNode<S>> tokens = new LinkedHashMap<>();
    private final Map<String, ArgumentNode<S, ?>> arguments = new LinkedHashMap<>();
    private final CompilerNode<S> redirect;
    private final RedirectModifier<S> modifier;
    private final boolean forks;

    protected CompilerNode(final CompilerNode<S> redirect, final RedirectModifier<S> modifier, final boolean forks) {
        this.redirect = redirect;
        this.modifier = modifier;
        this.forks = forks;
    }

    public Collection<CompilerNode<S>> getChildren() {
        return children.values();
    }

    public CompilerNode<S> getChild(final String name) {
        return children.get(name);
    }

    public CompilerNode<S> getRedirect() {
        return redirect;
    }

    public RedirectModifier<S> getRedirectModifier() {
        return modifier;
    }

    public void addChild(final CompilerNode<S> node) {
        if (node instanceof RootNode<S>) {
            throw new UnsupportedOperationException("Cannot add a RootCommandNode as a child to any other CommandNode");
        }

        final CompilerNode<S> child = children.get(node.getName());
        if (child != null) {
            // We've found something to merge onto
            for (final CompilerNode<S> grandchild : node.getChildren()) {
                child.addChild(grandchild);
            }
        } else {
            children.put(node.getName(), node);
            if (node instanceof TokenNode<S> tokenNode) {
                tokens.put(node.getName(), tokenNode);
            } else if (node instanceof ArgumentNode<S,?> argNode) {
                arguments.put(node.getName(), argNode);
            }
        }
    }

    protected abstract boolean isValidInput(final String input);

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CompilerNode)) return false;

        final CompilerNode<S> that = (CompilerNode<S>) o;

        return children.equals(that.children);
    }

    @Override
    public int hashCode() {
        return 31 * children.hashCode();
    }

    public abstract String getName();

    public abstract String getUsageText();

    public abstract void parse(TokenReader reader, CompilerContextBuilder<S> contextBuilder);

    public abstract CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) throws CommandSyntaxException;

    public abstract ArgumentBuilder<S, ?> createBuilder();

    protected abstract String getSortedKey();

    public Collection<? extends CompilerNode<S>> getRelevantNodes(final StringReader input) {
        if (!tokens.isEmpty()) {
            final int cursor = input.getCursor();
            while (input.canRead() && input.peek() != ' ') {
                input.skip();
            }
            final String text = input.getString().substring(cursor, input.getCursor());
            input.setCursor(cursor);
            final TokenNode<S> literal = tokens.get(text);
            if (literal != null) {
                return Collections.singleton(literal);
            } else {
                return arguments.values();
            }
        } else {
            return arguments.values();
        }
    }

    public boolean isFork() {
        return forks;
    }
}
