package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class VarNameSelectorWidget implements ExprCodeWidget {
    private @NotNull Status status = Status.UNKNOWN;
    private @Nullable String selected;

    @Override
    public @NotNull Type getType() {
        throw new IllegalAccessError("should not serialize VarNameSelector");
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.drawString(font, Component.literal(getVisualSelected()).withStyle(status.color), renderX, renderY, 0);
    }

    @Override
    public int getWidth(Font font) {
        return status == Status.UNKNOWN ? font.width("???") : font.width(selected);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public ExprCodeWidget copy() {
        return new VarNameSelectorWidget();
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        throw new IllegalAccessError("can not insert into var name selector widget");
    }

    @Override
    public CodeWidget getByName(String argName) {
        throw new IllegalAccessError("can not get from var name selector widget");
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {

    }

    @Override
    public void update(@Nullable MethodContext context) {
        if (context == null) //no method
            status = Status.UNKNOWN;
        else if (context.lvt.has(this.selected))
            status = Status.PRESENT;
        else
            status = Status.MISSING;
    }

    public @NotNull String getVisualSelected() {
        return selected != null ? selected : "???";
    }

    public void setSelected(@Nullable String selected) {
        this.selected = selected;
    }

    public @Nullable String getSelected() {
        return selected;
    }

    private enum Status {
        UNKNOWN(ChatFormatting.YELLOW),
        PRESENT(ChatFormatting.BLACK),
        MISSING(ChatFormatting.RED);

        private final ChatFormatting color;

        Status(ChatFormatting color) {
            this.color = color;
        }

        public ChatFormatting getColor() {
            return color;
        }
    }
}
