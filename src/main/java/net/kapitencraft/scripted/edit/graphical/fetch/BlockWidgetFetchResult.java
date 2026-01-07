package net.kapitencraft.scripted.edit.graphical.fetch;

import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;

import java.util.Map;
import java.util.regex.Matcher;

public record BlockWidgetFetchResult(boolean removed, int x, int y, BlockCodeWidget widget) {
    public static BlockWidgetFetchResult notRemoved(BlockCodeWidget exprWidget, int x, int y) {
        return new BlockWidgetFetchResult(false, x, y, exprWidget);
    }

    public BlockWidgetFetchResult setRemoved() {
        return new BlockWidgetFetchResult(true, this.x, this.y, this.widget);
    }

    public static BlockWidgetFetchResult fromExprList(int minWidth, int x, int y, Font font, BlockCodeWidget self, String translation, Map<String, ExprCodeWidget> expr) {
        if (x < minWidth) return BlockWidgetFetchResult.notRemoved(self, x, y);
        x -= minWidth;
        String inst = Language.getInstance().getOrDefault(translation);
        Matcher matcher = RenderHelper.VAR_TEXT_REGEX.matcher(inst);
        int j, l;
        for (j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String subElement = inst.substring(j, k);
            if (x < font.width(subElement))
                return BlockWidgetFetchResult.notRemoved(self, x, y);
            x -= font.width(subElement);
            String name = matcher.group(1);
            ExprCodeWidget widget = expr.get(name);
            if (x < widget.getWidth(font)) {
                BlockWidgetFetchResult result = widget.fetchAndRemoveHovered(x, y, font);
                if (result == null)
                    return BlockWidgetFetchResult.notRemoved(self, x, y);
                if (!result.removed())
                    expr.remove(name);
                return result.setRemoved();
            }
            x -= widget.getWidth(font);
        }
        return BlockWidgetFetchResult.notRemoved(self, x, y);
    }
}
