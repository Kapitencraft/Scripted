package net.kapitencraft.scripted.edit.text.node;

import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kapitencraft.scripted.edit.Token;
import net.kapitencraft.scripted.edit.text.TokenReader;
import net.kapitencraft.scripted.edit.text.builder.CompilerContextBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ArgumentNode<S, T> extends CompilerNode<S> {
    private final Function<String, T> mapper;
    private final Token.Type type;

    public ArgumentNode(CompilerNode<S> redirect, RedirectModifier<S> modifier, boolean forks, Function<String, T> mapper, Token.Type type) {
        super(redirect, modifier, forks);
        this.mapper = mapper;
        this.type = type;
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
        contextBuilder.add(mapper.apply(reader.getCurrent().value));
        reader.next();
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
