package net.kapitencraft.scripted.edit;

import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface RenderHelper {

    Pattern VAR_TEXT_REGEX = Pattern.compile("\\{([a-zA-Z0-9_]+)}"); //oooh pattern :pog:

    static void renderVisualText(GuiGraphics graphics, Font font, int x, int y, String key, Map<String, ExprCodeWidget> entries) {
        String inst = Language.getInstance().getOrDefault(key);
        Matcher matcher = VAR_TEXT_REGEX.matcher(inst);
        int j, l;
        for (j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String subElement = inst.substring(j, k);
            graphics.drawString(font, subElement, x, y, 0, false);
            x += font.width(subElement);
            String name = matcher.group(1);
            ExprCodeWidget widget = entries.get(name);

            widget.render(graphics, font, x, y - (widget.getHeight() - 8) / 2);
            x += widget.getWidth(font);
        }
        String subElement = inst.substring(j);
        graphics.drawString(font, subElement, x, y, 0, false);
    }

    static int getVisualTextWidth(Font font, String key, Map<String, ExprCodeWidget> map) {
        String inst = Language.getInstance().getOrDefault(key);
        int width = 0;
        Matcher matcher = VAR_TEXT_REGEX.matcher(inst);
        int j, l;
        for (j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String subElement = inst.substring(j, k);
            width += font.width(subElement);
            String name = matcher.group(1);
            ExprCodeWidget widget = map.get(name);
            width += widget.getWidth(font);
        }
        String subElement = inst.substring(j);
        width += font.width(subElement);
        return width + 1;
    }

    static void forPartialWidth(Font font, String key, Map<String, ExprCodeWidget> args, BiConsumer<String, Integer> offsets) {
        String inst = Language.getInstance().getOrDefault(key);
        int width = 0;
        Matcher matcher = VAR_TEXT_REGEX.matcher(inst);
        int j, l;
        for (j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String subElement = inst.substring(j, k);
            width += font.width(subElement);
            String name = matcher.group(1);
            offsets.accept(name, width);
            ExprCodeWidget widget = args.get(name);
            width += widget.getWidth(font);
        }
    }

    static int getPartialWidth(Font font, String key, Map<String, ExprCodeWidget> args, String paramToFind) {
        String inst = Language.getInstance().getOrDefault(key);
        int width = 0;
        Matcher matcher = VAR_TEXT_REGEX.matcher(inst);
        int j, l;
        for (j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String subElement = inst.substring(j, k);
            width += font.width(subElement);
            String name = matcher.group(1);
            if (Objects.equals(name, paramToFind)) {
                return width;
            }
            ExprCodeWidget widget = args.get(name);
            width += widget.getWidth(font);
        }
        throw new IllegalArgumentException("could not find argument " + paramToFind + " in key " + key + ": " + inst);
    }

    static void registerAllInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink, String translationKey, Map<String, ExprCodeWidget> args) {
        String inst = Language.getInstance().getOrDefault(translationKey);
        int width = 0;
        Matcher matcher = VAR_TEXT_REGEX.matcher(inst);
        int j, l;
        for (j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String subElement = inst.substring(j, k);
            width += font.width(subElement);
            String name = matcher.group(1);
            ExprCodeWidget widget = args.get(name);
            widget.registerInteractions(xOrigin + width, yOrigin, font, sink);
            width += widget.getWidth(font);
        }
    }
}
