package net.kapitencraft.scripted.edit.graphical.widgets;

import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.Nullable;

public interface Removable {

    @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font);
}