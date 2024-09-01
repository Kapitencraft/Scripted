package net.kapitencraft.scripted.edit;

import net.kapitencraft.kap_lib.client.widget.ScrollableWidget;
import net.kapitencraft.kap_lib.client.widget.text.MultiLineTextBox;
import net.kapitencraft.scripted.code.oop.Script;
import net.kapitencraft.scripted.edit.text.language.java.JavaCompiler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class EditWidget extends ScrollableWidget {
    private final MultiLineTextBox editor;
    private final Button button;

    public EditWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage, EditWidget old) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.editor = new MultiLineTextBox(Minecraft.getInstance().font, pX, pY, pWidth, pHeight - 20, old.editor, Component.empty());
        this.editor.setFocused(true);
        this.button = new PlainTextButton(pX + 1, pY + pHeight - 19, 20, 18, Component.translatable("scripted.edit"), pButton -> {
            Script script = JavaCompiler.compileScript(this.editor.getValue());
        }, Minecraft.getInstance().font);
    }

    @Override
    protected void updateScroll(boolean b) {
    }

    @Override
    protected int valueSize(boolean b) {
        return 0;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.editor.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.button.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {
        this.editor.updateWidgetNarration(pNarrationElementOutput);
    }
}
