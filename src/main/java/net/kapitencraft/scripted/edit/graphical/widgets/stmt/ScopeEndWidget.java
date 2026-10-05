package net.kapitencraft.scripted.edit.graphical.widgets.stmt;

import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public abstract class ScopeEndWidget extends StmtCodeWidget {

    @Override
    public void setChild(StmtCodeWidget child) {
        if (child != null)
            throw new IllegalAccessError("can not set child of scope ending widget");
    }

    @Override
    public void render(GuiGraphics graphics, Font font) {
        graphics.blitSprite(CodeWidgetSprites.BRANCH_END, x, y, getWidth(font), getHeight());
    }

    @Override
    public void collectConnectors(Font font, Consumer<Connector> collector) {
        //do not call super, as that would add the child connector, which doesn't exist for scope ending widgets
    }

    @Override
    public boolean ends() {
        return true;
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (x > this.getWidth(font) || y > this.getHeight())
            return null;
        return BlockWidgetFetchResult.notRemoved(this, x, y);
    }
}
