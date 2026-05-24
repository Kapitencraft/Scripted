package net.kapitencraft.scripted.edit.graphical;

import net.kapitencraft.scripted.edit.graphical.core.GraphicalEditor;
import net.kapitencraft.scripted.edit.graphical.selection.SelectionTab;
import net.kapitencraft.scripted.registry.ModRegistries;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;

public class BlocklyEditScreen extends Screen {
    private GraphicalEditor graphicalEditor;

    public BlocklyEditScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        Registry<SelectionTab> tabs = this.minecraft.level.registryAccess().registryOrThrow(ModRegistries.Keys.SELECTION_TABS);

        GraphicalEditor original = graphicalEditor;
        this.addRenderableWidget(this.graphicalEditor = new GraphicalEditor(10, 10, width - 20, height - 20, Component.literal("hi"), this.font, tabs));
        this.graphicalEditor.updateContentFrom(original);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.children().forEach(l -> l.mouseMoved(mouseX, mouseY));
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void onClose() {
        //Compiler
        super.onClose();
    }
}
