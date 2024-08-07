package net.kapitencraft.scripted.edit.text.java;

import com.mojang.brigadier.context.StringRange;
import net.kapitencraft.kap_lib.client.widget.text.IFormatter;
import net.kapitencraft.kap_lib.client.widget.text.ISuggestion;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class JavaIDE implements IFormatter, ISuggestion {
    private final List<List<StringRangeWithToken>> formatted = new ArrayList<>();

    @Override
    public FormattedCharSequence format(String s, int i) {
        List<StringRangeWithToken> lineContent = formatted.get(i);
        return null;
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

    private static class StringRangeWithToken extends StringRange {

        public StringRangeWithToken(int start, int end) {
            super(start, end);
        }
    }
}
