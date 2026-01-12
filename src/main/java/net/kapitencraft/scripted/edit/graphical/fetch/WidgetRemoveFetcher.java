package net.kapitencraft.scripted.edit.graphical.fetch;

import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.Nullable;

public interface WidgetRemoveFetcher {

    @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font);
}