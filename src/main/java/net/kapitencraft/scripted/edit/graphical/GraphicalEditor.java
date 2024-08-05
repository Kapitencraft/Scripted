package net.kapitencraft.scripted.edit.graphical;

import net.kapitencraft.kap_lib.client.widget.ScrollableWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class GraphicalEditor extends ScrollableWidget {


    public GraphicalEditor(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage);
    }

    @Override
    protected void updateScroll(boolean b) {

    }

    @Override
    protected int valueSize(boolean b) {
        return 0;
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
        pGuiGraphics.pose().translate(this.scrollX, this.scrollY, 0);

        pGuiGraphics.disableScissor();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}
