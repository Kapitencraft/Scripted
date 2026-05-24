package net.kapitencraft.scripted.edit.text;

import net.kapitencraft.kap_lib.core.client.widget.text.IDE;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class JavaIDE implements IDE {
    private static final List<String> KEYWORDS = List.of(
            "abstract", "annotation",
                    "bool", "break",
                    "case", "catch", "char", "class", "continue",
                    "default", "double",
                    "else", "enum", "extends",
                    "false", "final", "finally", "float", "for",
                    "if", "implements", "import", "int", "interface",
                    "new", "num",
                    "package",
                    "return",
                    "static", "super", "switch",
                    "this", "trace", "true", "try",
                    "void",
                    "while"
    );

    @Override
    public FormattedCharSequence format(String text, int lineIndex) {
        return FormattedCharSequence.forward(text, Style.EMPTY);
    }

    @Override
    public List<String> suggestions(String in) {
        return KEYWORDS;
    }

    @Override
    public void lineCreated(int index) {

    }

    @Override
    public void lineModified(int index, String content) {

    }

    @Override
    public void lineRemoved(int index) {

    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
