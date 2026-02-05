package net.kapitencraft.scripted.edit.graphical.widgets.io;

import net.kapitencraft.kap_lib.core.client.widget.PositionedWidget;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.widgets.block.IfWidget;
import net.minecraft.client.gui.GuiGraphics;

public class ModifyIfBranchesWidget extends PositionedWidget {
    private final IfWidget owner;

    protected ModifyIfBranchesWidget(int x, int y, int width, int height, IfWidget owner) {
        super(x, y, width, height);
        this.owner = owner;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blitSprite(CodeWidgetSprites.METHOD_HEAD, x + 2, y + 2, 10, 10); //TODO we do do the do
    }
}
