package net.kapitencraft.scripted.edit;

import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface RenderHelper {

    Pattern VAR_TEXT_REGEX = Pattern.compile("%\\{([a-zA-Z0-9_]+)}%"); //oooh pattern :pog:

    static int renderVisualText(GuiGraphics graphics, Font font, int x, int y, String key, Map<String, ExprCodeWidget> entries) {
        String inst = Language.getInstance().getOrDefault(key);
        Matcher matcher = VAR_TEXT_REGEX.matcher(inst);
        int j, l;
        for(j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String subElement = inst.substring(j, k);
            graphics.drawString(font, subElement, x, y, 0, false);
            x += font.width(subElement);
            String name = matcher.group(1);
            ExprCodeWidget widget = entries.get(name);
            widget.render(graphics, font, x, y);
            x += widget.getWidth(font);
        }
        String subElement = inst.substring(j);
        graphics.drawString(font, subElement, x, y, 0, false);
        x += font.width(subElement);
        return x + 1;
    }

    @Deprecated
    static void renderExprList(GuiGraphics graphics, Font font, int renderX, int renderY, List<ExprCodeWidget> children) {
        int i = 0;
        for (ExprCodeWidget child : children) {
            child.render(graphics, font, renderX + i, renderY);
            i += child.getWidth(font);
        }
    }

    static int getVisualTextWidth(Font font, String key, Map<String, ExprCodeWidget> map) {
        String inst = Language.getInstance().getOrDefault(key);
        int width = 0;
        Matcher matcher = VAR_TEXT_REGEX.matcher(inst);
        int j, l;
        for(j = 0; matcher.find(j); j = l) {
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
}
