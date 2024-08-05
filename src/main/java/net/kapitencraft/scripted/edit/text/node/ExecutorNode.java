package net.kapitencraft.scripted.edit.text.node;

import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kapitencraft.scripted.edit.text.TokenReader;
import net.kapitencraft.scripted.edit.text.builder.CompilerContextBuilder;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ExecutorNode<S, T> extends CompilerNode<S> {
    public final Function<CompilerContextBuilder<S>, T> creator;
    public final HashMap<CompilerNode<S>, BiConsumer<CompilerContextBuilder<S>, T>> applier = new HashMap<>();

    public ExecutorNode(CompilerNode<S> redirect, RedirectModifier<S> modifier, boolean forks, Function<CompilerContextBuilder<S>, T> creator, HashMap<CompilerNode<S>, BiConsumer<CompilerContextBuilder<S>, T>> map) {
        super(redirect, modifier, forks);
        this.creator = creator;
        this.applier.putAll(map);
    }

    @Override
    protected boolean isValidInput(String input) {
        return false;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getUsageText() {
        return "";
    }

    @Override
    public void parse(TokenReader reader, CompilerContextBuilder<S> contextBuilder) {
        contextBuilder.add(creator.apply(contextBuilder));
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return null;
    }

    @Override
    public ArgumentBuilder<S, ?> createBuilder() {
        return null;
    }

    @Override
    protected String getSortedKey() {
        return "";
    }
}
