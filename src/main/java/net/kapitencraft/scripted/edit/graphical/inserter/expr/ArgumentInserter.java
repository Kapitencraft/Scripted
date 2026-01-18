package net.kapitencraft.scripted.edit.graphical.inserter.expr;

import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;

public class ArgumentInserter implements ExprGhostInserter {
    private final String argName;
    private final BiConsumer<String, ExprCodeWidget> valueSink;
    private final ExprCodeWidget original;

    public ArgumentInserter(String argName, BiConsumer<String, ExprCodeWidget> valueSink, ExprCodeWidget original) {
        this.argName = argName;
        this.valueSink = valueSink;
        this.original = original;
    }

    @Nullable
    public static GhostInserter create(int x, int y, Font font, String translation, BiConsumer<String, ExprCodeWidget> valueSink, Map<String, ExprCodeWidget> entry) {
        if (x < 0) return null;
        String inst = Language.getInstance().getOrDefault(translation);
        Matcher matcher = RenderHelper.VAR_TEXT_REGEX.matcher(inst);
        int j, l;
        for (j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String subElement = inst.substring(j, k);
            x -= font.width(subElement);
            String name = matcher.group(1);
            ExprCodeWidget widget = entry.get(name);
            int w = widget.getWidth(font);
            if (x < 15)
                return new ArgumentInserter(name, valueSink, widget);
            if (x < w)
                return widget.getGhostWidgetTarget(x, y, font, false);
            x -= w;
        }
        return null;
    }

    @Override
    public void insert(CodeWidget target) {
        this.valueSink.accept(argName, (ExprCodeWidget) target);
    }

    @Override
    public ExprCodeWidget getOriginal() {
        return original;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArgumentInserter aI && aI.argName.equals(this.argName);
    }
}
