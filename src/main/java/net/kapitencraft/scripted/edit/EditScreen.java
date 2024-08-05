package net.kapitencraft.scripted.edit;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EditScreen extends Screen {
    private EditWidget widget;

    protected EditScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new EditWidget(10, 10, this.width-20, this.height-20, Component.empty(), null));
    }
}
