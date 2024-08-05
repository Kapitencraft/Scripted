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
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TokenNode<S> extends CompilerNode<S> {
    private final Token.Type type;
    private final @Nullable String name;

    public TokenNode(CompilerNode<S> redirect, RedirectModifier<S> modifier, boolean forks, Token.Type type, @Nullable String name) {
        super(redirect, modifier, forks);
        this.type = type;
        this.name = name;
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
