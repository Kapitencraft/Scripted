package net.kapitencraft.scripted.edit.graphical.widgets;

import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public interface CodeWidget {

    void render(GuiGraphics graphics, Font font, int renderX, int renderY);

    int getHeight();

    int getWidth(Font font);

    CodeWidget copy();

    //TODO
    GhostInserter getGhostWidgetTarget(int x, int y, Font font, boolean isBlock);

    void update(@Nullable MethodContext context);
}
