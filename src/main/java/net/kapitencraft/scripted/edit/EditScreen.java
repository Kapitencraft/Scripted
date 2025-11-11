package net.kapitencraft.scripted.edit;

import net.kapitencraft.scripted.edit.graphical.GraphicalEditor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EditScreen extends Screen {
    protected EditScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new GraphicalEditor(10, 10, width - 20, height - 20, Component.literal("hi"), this.font));
        //this.addRenderableWidget(box = Util.make(() -> {
        //    MultiLineTextBox box = new MultiLineTextBox(this.font, 10, 10, this.width-20, this.height-20, this.box, null);
        //    box.setLineRenderType(MultiLineTextBox.LineRenderType.EVERY);
        //    return box;
        //}));
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }
}
