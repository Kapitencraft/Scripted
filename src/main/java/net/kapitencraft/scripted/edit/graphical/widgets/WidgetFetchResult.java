package net.kapitencraft.scripted.edit.graphical.widgets;

import net.kapitencraft.scripted.edit.RenderHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public record WidgetFetchResult(boolean removed, int x, int y, CodeWidget widget) {
    public static WidgetFetchResult notRemoved(CodeWidget exprWidget, int x, int y) {
        return new WidgetFetchResult(false, x, y, exprWidget);
    }

    public WidgetFetchResult setRemoved() {
        return new WidgetFetchResult(true, this.x, this.y, this.widget);
    }

    public static WidgetFetchResult fromExprList(int minWidth, int x, int y, Font font, CodeWidget self, String translation, Map<String, CodeWidget> args) {
        return fromExprList(minWidth, x, y, font, self, translation, args, true);
    }

    public static WidgetFetchResult fromExprList(int minWidth, int x, int y, Font font, CodeWidget self, String translation, Map<String, CodeWidget> expr, boolean remove) {
        if (x < minWidth) return WidgetFetchResult.notRemoved(self, x, y);
        x -= minWidth;
        String inst = Language.getInstance().getOrDefault(translation);
        Matcher matcher = RenderHelper.VAR_TEXT_REGEX.matcher(inst);
        int j, l;
        for (j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String subElement = inst.substring(j, k);
            if (x < font.width(subElement))
                return WidgetFetchResult.notRemoved(self, x, y);
            x -= font.width(subElement);
            String name = matcher.group(1);
            CodeWidget widget = expr.get(name);
            if (x < widget.getWidth(font)) {
                WidgetFetchResult result = widget.fetchAndRemoveHovered(x, y, font);
                if (result == null)
                    return null;
                if (!result.removed())
                    expr.remove(name);
                return result.setRemoved();
            }
            x -= widget.getWidth(font);
        }
        return WidgetFetchResult.notRemoved(self, x, y);
    }
}
