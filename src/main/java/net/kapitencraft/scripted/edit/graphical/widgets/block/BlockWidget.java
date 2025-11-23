package net.kapitencraft.scripted.edit.graphical.widgets.block;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.Removable;
import net.kapitencraft.scripted.edit.graphical.widgets.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public abstract class BlockWidget implements CodeWidget, Removable {
    private BlockWidget child;

    public void setChild(BlockWidget child) {
        this.child = child;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        if (this.child != null)
            this.child.render(graphics, font, renderX, renderY + getHeight());
    }

    public BlockWidget getChild() {
        return child;
    }

    protected WidgetFetchResult fetchChildRemoveHovered(int x, int y, Font font) {
        WidgetFetchResult result = this.child.fetchAndRemoveHovered(x, y, font);
        if (!result.removed())
            this.setChild(null);
        return result.setRemoved();
    }

    public interface Builder<T extends BlockWidget> {

        T build();
    }
}
