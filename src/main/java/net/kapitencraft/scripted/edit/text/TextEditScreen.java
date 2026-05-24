package net.kapitencraft.scripted.edit.text;

import net.kapitencraft.kap_lib.core.client.widget.text.MultiLineTextBox;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TextEditScreen extends Screen {

    public TextEditScreen() {
        super(Component.empty());
    }

    private MultiLineTextBox box;

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(box = Util.make(() -> {
            MultiLineTextBox box = new MultiLineTextBox(this.font, 10, 10, this.width-20, this.height-20, this.box, Component.translatable("scripted.text_ide"));
            box.setLineRenderType(MultiLineTextBox.LineRenderType.EVERY);
            box.setIDE(new JavaIDE());
            return box;
        }));
    }
}