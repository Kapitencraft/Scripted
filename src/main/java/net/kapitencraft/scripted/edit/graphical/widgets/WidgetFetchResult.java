package net.kapitencraft.scripted.edit.graphical.widgets;

import net.minecraft.client.gui.Font;

import java.util.List;

public record WidgetFetchResult(boolean removed, int x, int y, CodeWidget widget) {
    public static WidgetFetchResult notRemoved(CodeWidget exprWidget, int x, int y) {
        return new WidgetFetchResult(false, x, y, exprWidget);
    }

    public WidgetFetchResult setRemoved() {
        return new WidgetFetchResult(true, this.x, this.y, this.widget);
    }

    public static WidgetFetchResult fromExprList(int minWidth, int x, int y, Font font, CodeWidget self, List<CodeWidget> expr) {
        return fromExprList(minWidth, x, y, font, self, expr, true);
    }

    public static WidgetFetchResult fromExprList(int minWidth, int x, int y, Font font, CodeWidget self, List<CodeWidget> expr, boolean remove) {
        if (x < minWidth) return WidgetFetchResult.notRemoved(self, x, y);
        x -= minWidth;
        int i = 0;
        while (i < expr.size()) {
            CodeWidget widget = expr.get(i);
            int widgetWidth = widget.getWidth(font);
            if (x < widgetWidth) {
                if (widget instanceof Removable removable) {
                    WidgetFetchResult result = removable.fetchAndRemoveHovered(x, y, font);
                    if (remove && !result.removed()) {
                        expr.remove(i); //assuming it was removed directly from this expr
                    }
                    return result.setRemoved();
                }
                return WidgetFetchResult.notRemoved(self, x + minWidth, y); //the only non-removable widget is a text widget, which is bound to it's surrounding block / expr
            }
            x -= widgetWidth;
            i++;
        }
        return WidgetFetchResult.notRemoved(self, x + minWidth, y);
    }
}
