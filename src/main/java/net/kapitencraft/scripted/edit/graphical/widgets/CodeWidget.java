package net.kapitencraft.scripted.edit.graphical.widgets;

import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface CodeWidget {

    void render(GuiGraphics graphics, Font font, int renderX, int renderY);

    int getHeight();

    int getWidth(Font font);

    CodeWidget copy();

    //TODO
    GhostInserter getGhostWidgetTarget(int x, int y, Font font, boolean isBlock);

    void update(@Nullable MethodContext context);

    @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font);

    void registerInteractions(int rX, int rY, int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink);
}