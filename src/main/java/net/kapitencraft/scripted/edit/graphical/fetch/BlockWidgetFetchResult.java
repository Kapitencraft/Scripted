package net.kapitencraft.scripted.edit.graphical.fetch;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;

public record BlockWidgetFetchResult(boolean removed, int x, int y,
                                     BlockCodeWidget widget) implements WidgetFetchResult {
    public static BlockWidgetFetchResult notRemoved(BlockCodeWidget exprWidget, int x, int y) {
        return new BlockWidgetFetchResult(false, x, y, exprWidget);
    }

    public BlockWidgetFetchResult setRemoved() {
        return new BlockWidgetFetchResult(true, this.x, this.y, this.widget);
    }
}
