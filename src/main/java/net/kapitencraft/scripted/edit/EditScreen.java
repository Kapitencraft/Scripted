package net.kapitencraft.scripted.edit;

import net.kapitencraft.scripted.edit.graphical.GraphicalEditor;
import net.kapitencraft.scripted.edit.graphical.selection.SelectionTab;
import net.kapitencraft.scripted.registry.ModRegistries;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;

public class EditScreen extends Screen {
    protected EditScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        Registry<SelectionTab> tabs = this.minecraft.level.registryAccess().registryOrThrow(ModRegistries.Keys.SELECTION_TABS);
        this.addRenderableWidget(new GraphicalEditor(10, 10, width - 20, height - 20, Component.literal("hi"), this.font, tabs));
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

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.children().forEach(l -> l.mouseMoved(mouseX, mouseY));
        super.mouseMoved(mouseX, mouseY);
    }
}
