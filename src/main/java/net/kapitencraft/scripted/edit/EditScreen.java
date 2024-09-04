package net.kapitencraft.scripted.edit;

import net.kapitencraft.kap_lib.client.widget.text.MultiLineTextBox;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EditScreen extends Screen {
    private MultiLineTextBox box;

    protected EditScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        this.addRenderableWidget(box = Util.make(() -> {
            MultiLineTextBox box = new MultiLineTextBox(this.font, 10, 10, this.width-20, this.height-20, this.box, null);
            box.setLineRenderType(MultiLineTextBox.LineRenderType.EVERY);
            return box;
        }));
    }
}
