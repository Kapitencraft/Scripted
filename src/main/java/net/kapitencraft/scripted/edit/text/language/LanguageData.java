package net.kapitencraft.scripted.edit.text.language;

import net.kapitencraft.scripted.code.LanguageProvider;
import net.kapitencraft.scripted.code.oop.core.Object;
import net.kapitencraft.scripted.edit.Token;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LanguageData<P extends LanguageProvider, I extends LanguageIDE<T, P>, T extends Tokenizer, C extends Compiler> {
    private final P langProvider;
    private final I ide;
    private final T tokenizer;
    private final Function<List<Token>, C> compilerCreator;

    public LanguageData(P langProvider, BiFunction<T, P, I> ideProvider, T tokenizer, Function<List<Token>, C> compilerCreator) {
        this.langProvider = langProvider;
        this.tokenizer = tokenizer;
        this.compilerCreator = compilerCreator;
        this.ide = ideProvider.apply(tokenizer, langProvider);
    }


    public I getIDE() {
        return ide;
    }

    public Object compile(String string) {
        return compilerCreator.apply(tokenizer.tokenize(string)).castObject();
    }
}
