package net.kapitencraft.scripted.edit;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EditScreen extends Screen {
    protected EditScreen() {
        super(Component.translatable("edit_screen.title"));
    }
}
