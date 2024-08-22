package net.kapitencraft.scripted.edit.text.language.java;

import com.mojang.brigadier.context.StringRange;
import net.kapitencraft.scripted.edit.Token;
import net.kapitencraft.scripted.edit.text.language.LanguageIDE;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class JavaIDE extends LanguageIDE<JavaTokenizer, JavaLanguageProvider> {
    private final List<List<StringRangeWithToken>> formatted = new ArrayList<>();

    public JavaIDE(JavaTokenizer tokenizer, JavaLanguageProvider provider) {
        super(tokenizer, provider);
    }

    @Override
    public FormattedCharSequence format(String s, int i) {
        List<StringRangeWithToken> lineContent = formatted.get(i);
        return FormattedCharSequence.fromList(lineContent.stream()
                .map(range -> FormattedCharSequence.forward(s.substring(range.getStart(), range.getEnd()), this.provider.getFormatting(range.type))).toList()
        );
    }

    @Override
    public List<String> suggestions(String s) {
        return List.of();
    }

    @Override
    public void setFocused(boolean b) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void lineCreated(int i) {
        this.formatted.add(i, new ArrayList<>());
    }

    @Override
    public void lineModified(int i, String s) {
        this.reapply(i, s);
    }

    private void reapply(int i, String s) {
        List<Token> tokens = tokenizer.tokenize(s);
    }

    @Override
    public void lineRemoved(int i) {
        this.formatted.remove(i);
    }

    private static class StringRangeWithToken extends StringRange {
        private final Token.Type type;

        public StringRangeWithToken(int start, int end, Token.Type type) {
            super(start, end);
            this.type = type;
        }
    }
}
