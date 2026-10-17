package net.kapitencraft.scripted.edit.graphical.widgets.io;

import net.kapitencraft.kap_lib.core.client.widget.PositionedWidget;
import net.kapitencraft.kap_lib.core.helpers.MathHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExtensibleSelectStringWidget extends PositionedWidget {
    private static final Component SELECT_PANEL = Component.translatable("mco.template.button.select");
    private static final Component ADD_PANEL = Component.translatable("scripted.widget.add");

    private final EditBox box;
    private final Font font;
    private final List<String> values;
    private final Function<String, Component> nameExtractor;
    private final Consumer<String> valueSink;
    private final Component title;
    private final int titleWidth;
    private String selected;

    public ExtensibleSelectStringWidget(int x, int y, int width, int height, Font font, List<String> values, Function<String, Component> nameExtractor, Consumer<String> valueSink, Component title) {
        super(x, y, width, height);
        this.font = font;
        this.values = values;
        this.nameExtractor = nameExtractor;
        this.valueSink = valueSink;
        this.title = title;
        this.titleWidth = this.font.width(title);
        this.box = new EditBox(this.font, (int) (width / 2.5), 14, Component.empty());
        this.box.setPosition(x, getMaxY() - 14);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.fill(this.x, this.y, this.getMaxX(), this.getMaxY(), 0xFF404040);
        pGuiGraphics.drawCenteredString(font, title, this.x + width / 2, this.y + 1, 0xFFFFFF);
        pGuiGraphics.enableScissor(this.x + 2, this.y + 12, this.getMaxX() - 2, this.getMaxY() - 16);
        int selectedIndex = (pMouseY - this.y - 12) / 10;
        for (int i = 0; i < values.size(); i++) {
            String t = values.get(i);
            int y = this.y + 12 + i * 10;
            pGuiGraphics.fill(this.x + 2, y - 1, this.getMaxX() - 3, y + 9, Objects.equals(selected, t) ? 0xFF808080 : i == selectedIndex ? 0xFF707070 : 0xFF606060);
            pGuiGraphics.drawString(font, nameExtractor.apply(t), this.x + 3, y, 0xFFFFFF);
        }
        pGuiGraphics.disableScissor();
        this.box.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.drawString(this.font, ADD_PANEL, this.x + this.box.getWidth(), this.getMaxY() - 12, 0xFFFFFF);
        pGuiGraphics.drawString(this.font, SELECT_PANEL, this.getMaxX() - (font.width(SELECT_PANEL) + 2), this.getMaxY() - 12, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        //pMouseY = this.y + 12 + i * 10
        int selectedIndex = ((int) pMouseY - this.y - 12) / 10;
        if (MathHelper.validIndex(values, selectedIndex)) {
            selected = values.get(selectedIndex);
            return true;
        }

        int selectPos = this.getMaxX() - (font.width(SELECT_PANEL) + 2);
        if (MathHelper.is2dBetween(pMouseX, pMouseY, selectPos - 1, this.getMaxY() - 12, selectPos + titleWidth + 1, this.getMaxY() - 2)) {
            if (this.selected != null) {
                this.valueSink.accept(selected);
                this.selected = null;
                return true;
            }
        } else if (MathHelper.is2dBetween(pMouseX, pMouseY, this.x + this.box.getWidth(), this.getMaxY() - 12, this.x + this.box.getWidth() + font.width(ADD_PANEL) + 1, this.getMaxY() - 2)) {
            String value = this.box.getValue();
            this.values.add(value);
            this.selected = value;
            return true;
        } else if (MathHelper.is2dBetween(pMouseX, pMouseY, this.x, this.getMaxY() - 14, this.x + this.box.getWidth(), this.getMaxY()))
            this.box.setFocused(true);

        return this.box.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.box.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.box.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return this.box.keyReleased(keyCode, scanCode, modifiers);
    }
}
