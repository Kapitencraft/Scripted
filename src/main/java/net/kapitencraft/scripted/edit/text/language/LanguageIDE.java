package net.kapitencraft.scripted.edit.text.language;

import net.kapitencraft.kap_lib.client.widget.text.IDE;
import net.kapitencraft.scripted.code.LanguageProvider;

public abstract class LanguageIDE<T extends Tokenizer, P extends LanguageProvider> implements IDE {

    protected final T tokenizer;
    protected final P provider;

    protected LanguageIDE(T tokenizer, P provider) {
        this.tokenizer = tokenizer;
        this.provider = provider;
    }
}
