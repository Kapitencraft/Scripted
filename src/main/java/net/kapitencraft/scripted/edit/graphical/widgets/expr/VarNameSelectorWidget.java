package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VarNameSelectorWidget implements ExprCodeWidget {
    private Status status;
    private @Nullable String selected;

    @Override
    public @NotNull Type getType() {
        throw new IllegalAccessError("should not serialize VarNameSelector");
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        String toRender;
        if (status == Status.UNKNOWN)
            toRender = "???";
        else
            toRender = selected;
        graphics.drawString(font, toRender, renderX, renderY, 0);
    }

    @Override
    public int getWidth(Font font) {
        return status == Status.UNKNOWN ? font.width("???") : font.width(selected);
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public ExprCodeWidget copy() {
        return new VarNameSelectorWidget();
    }

    @Override
    public GhostInserter getGhostWidgetTarget(int x, int y, Font font, boolean isBlock) {
        return null;
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }

    @Override
    public void update(@Nullable MethodContext context) {
        if (context == null || selected == null) //no method
            status = Status.UNKNOWN;
        else if (context.lvt.has(this.selected))
            status = Status.PRESENT;
        else
            status = Status.MISSING;
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
