package net.kapitencraft.scripted.edit.graphical.widgets.io;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.kap_lib.core.client.widget.PositionedWidget;
import net.kapitencraft.kap_lib.core.helpers.MathHelper;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.VarRefWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.stmt.HeadWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CreateMethodWidget extends PositionedWidget {
    private static final Component TITLE = Component.translatable("scripted.widget.create_method");
    private static final Component CREATE = Component.translatable("scripted.widget.create");
    private final Font font;
    private final EditBox nameBox;
    private final Button createButton;
    private final List<Pair<String, ExprCategory>> params = new ArrayList<>();

    public CreateMethodWidget(int x, int y, int height, Font font, Consumer<HeadWidget> sink) {
        super(x, y, 100, height);
        this.font = font;
        this.nameBox = new EditBox(font, 92, 14, Component.empty());
        this.nameBox.setFocused(true);
        this.nameBox.setPosition(x + 4, y + 14);
        this.nameBox.setHint(Component.translatable("scripted.widget.set_method_name"));
        int buttonWidth = font.width(CREATE) + 4;
        this.createButton = Button.builder(CREATE, b -> sink.accept(createWidget()))
                .bounds(x + 98 - buttonWidth, y + height - 17, buttonWidth, 15)
                .build();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(this.x, this.y, getMaxX(), getMaxY(), 0xFF090909);
        guiGraphics.drawString(font, TITLE, this.x +  1, this.y + 1, -1);
        this.nameBox.render(guiGraphics, mouseX, mouseY, partialTick);
        this.createButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public HeadWidget createWidget() {
        return new HeadWidget(
                null, this.nameBox.getValue(),
                this.params.stream().map(p -> new VarRefWidget(p.getFirst(), p.getSecond())).toList()
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathHelper.is2dBetween(
                mouseX, mouseY,
                this.createButton.getX(), this.createButton.getY(),
                this.createButton.getX() + 92,
                this.createButton.getY() + 14
        )) {
            return createButton.mouseClicked(mouseX, mouseY, button);
        }
        if (MathHelper.is2dBetween(
                mouseX, mouseY,
                x + 4, y + 14,
                x + 4 + this.nameBox.getWidth(),
                y + 29
        )) {
            return nameBox.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return nameBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return nameBox.charTyped(codePoint, modifiers);
    }
}
