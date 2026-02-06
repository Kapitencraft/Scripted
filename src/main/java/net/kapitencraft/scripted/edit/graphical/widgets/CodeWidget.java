package net.kapitencraft.scripted.edit.graphical.widgets;

import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface CodeWidget {

    void render(GuiGraphics graphics, Font font, int renderX, int renderY);

    int getHeight();

    int getWidth(Font font);

    //region IO
    CodeWidget copy();

    void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj);

    CodeWidget getByName(String argName);

    @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font);

    void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector);
    //endregion IO

    void update(@Nullable MethodContext context);

    void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink);
}