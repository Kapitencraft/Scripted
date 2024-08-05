package net.kapitencraft.scripted.edit.text.node;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kapitencraft.scripted.edit.text.TokenReader;
import net.kapitencraft.scripted.edit.text.builder.CompilerContextBuilder;

import java.util.concurrent.CompletableFuture;

public class RootNode<S> extends CompilerNode<S> {

    public RootNode() {
        super(null, null, false);
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
