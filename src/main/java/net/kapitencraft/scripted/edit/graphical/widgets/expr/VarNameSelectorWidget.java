package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.InteractionData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class VarNameSelectorWidget implements ExprCodeWidget {
    private static final ResourceLocation ARROW = ResourceLocation.withDefaultNamespace("transferable_list/move_down");

    private int x, y;
    private @NotNull Status status = Status.UNKNOWN;
    private ExprCategory category = ExprCategory.OTHER;
    private @Nullable String selected = "something";
    private boolean createVar;

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public @NotNull Type getType() {
        throw new IllegalAccessError("should not serialize VarNameSelector");
    }

    @Override
    public void render(GuiGraphics graphics, Font font) {
        int x = this.x;
        int y = this.y;
        graphics.blitSprite(CodeWidgetSprites.GENERIC_EXPR, x, y, getWidth(font), getHeight());
        graphics.pose().pushPose();
        graphics.pose().translate(x + font.width(getVisualSelected()) - 6, y - 8, 0);
        graphics.pose().scale(2, 2, 1);
        graphics.blitSprite(ARROW, 0, 0, 10, 10);
        graphics.pose().popPose();
        graphics.drawString(font, Component.literal(getVisualSelected()).withStyle(status.color), x + 2, y + 3, 0, false);
    }

    @Override
    public int getWidth(Font font) {
        return font.width(getVisualSelected()) + 4 + 12;
    }

    @Override
    public int getHeight() {
        return 14;
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
    public CodeWidget getByName(String arg) {
        throw new IllegalAccessError("can not get from var name selector widget");
    }

    @Override
    public void collectConnectors(Font font, Consumer<Connector> collector) {
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }

    @Override
    public void registerInteractions(Font font, Consumer<CodeInteraction> sink) {
        sink.accept(new SelectVarNameInteraction(x, y, this.getWidth(font), this.getHeight()));
    }

    public void setCreate(boolean createVar) {
        this.createVar = createVar;
    }

    public boolean doesCreateVar() {
        return createVar;
    }

    public ExprCategory getCategory() {
        return this.category;
    }

    private static class SelectVarNameInteraction extends CodeInteraction {
        //TODO

        protected SelectVarNameInteraction(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void onClick(int mouseX, int mouseY, InteractionData callbacks) {
            //callbacks.openWidget();
        }
    }

    @Override
    public void update(@Nullable MethodContext context, Font font, int x, int y) {
        this.x = x;
        this.y = y;
        if (context == null) //no method
            status = Status.UNKNOWN;
        else if (createVar) {
            status = context.lvt.add(this.selected, this.category) ? Status.PRESENT : Status.MISSING;
        } else if (context.lvt.has(this.selected)) {
            status = Status.PRESENT;
            this.category = context.lvt.getType(this.selected);
        } else
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
