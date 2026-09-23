package net.kapitencraft.scripted.edit;

import net.kapitencraft.kap_lib.core.client.widget.text.MultiLineTextBox;
import net.kapitencraft.scripted.edit.graphical.core.GraphicalEditor;
import net.kapitencraft.scripted.edit.graphical.selection.SelectionTab;
import net.kapitencraft.scripted.registry.ModRegistries;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;

public class EditScreen extends Screen {
    private GraphicalEditor graphicalEditor;
    private boolean updateForOverview = false;

    protected EditScreen() {
        super(Component.empty());
    }

    MultiLineTextBox box;

    @Override
    protected void init() {
        Registry<SelectionTab> tabs = this.minecraft.level.registryAccess().registryOrThrow(ModRegistries.Keys.SELECTION_TABS);

        GraphicalEditor original = graphicalEditor;
        this.addRenderableWidget(this.graphicalEditor = new GraphicalEditor(10, 10, width - 20, height - 20, Component.literal("hi"), this.font, tabs));
        this.graphicalEditor.updateContentFrom(original);
        if (updateForOverview) {
            this.graphicalEditor.setViewData(12, -129.5f, -35.33f);
            updateForOverview = false;
        }

        //this.addRenderableWidget(box = Util.make(() -> {
        //    MultiLineTextBox box = new MultiLineTextBox(this.font, 10, 10, this.width-20, this.height-20, this.box, null);
        //    box.setLineRenderType(MultiLineTextBox.LineRenderType.EVERY);
        //    return box;
        //}));
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

    public static EditScreen forOverview() {
        EditScreen editScreen = new EditScreen();
        editScreen.updateForOverview = true;
        return editScreen;
    }
}
