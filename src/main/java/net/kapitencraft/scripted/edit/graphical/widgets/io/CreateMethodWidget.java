package net.kapitencraft.scripted.edit.graphical.widgets.io;

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
    //TODO convert params to actual CRs
    private final List<ParamEntry> params = new ArrayList<>();

    public CreateMethodWidget(int x, int y, int height, Font font, Consumer<HeadWidget> sink) {
        super(x, y, 100, height);
        this.font = font;
        this.nameBox = new EditBox(font, 92, 14, Component.empty());
        this.nameBox.setFocused(true);
        this.nameBox.setPosition(x + 4, y + 12);
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
        guiGraphics.enableScissor(this.x + 2, this.y + 30, this.getMaxX() - 2, this.getMaxY() - 12);

        List<ParamEntry> pairs = this.params;
        for (int i = 0; i < pairs.size(); i++) {
            ParamEntry param = pairs.get(i);
            int y = this.y + 30 + i * 15;
            guiGraphics.fill(this.x + 2, y - 1, this.getMaxX() - 2, y + 14, 0xFF808080);
            param.nameSelector.render(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.drawString(this.font, param.category.name(), this.getMaxX() - this.font.width(param.category.name()) - 3, y, -1 + 7, false);
        }
        int y = this.y + 30 + pairs.size() * 15;
        guiGraphics.drawString(font, "+", this.x + 3, y, -1);

        guiGraphics.disableScissor();
    }

    public HeadWidget createWidget() {
        return new HeadWidget(
                null, this.nameBox.getValue(),
                this.params.stream().map(p -> new VarRefWidget(p.getName(), p.category)).toList()
        );
    }

    private static class ParamEntry {
        private final EditBox nameSelector;
        private ExprCategory category;

        private ParamEntry(Font font) {
            this.nameSelector = new EditBox(font, 30, 12, Component.translatable("scripted.widget.select_param_name"));
            this.category = ExprCategory.OTHER;
        }

        private void update(int idx, int x, int y) {
            this.nameSelector.setPosition(x + 3, y + idx * 15 + 30);
        }

        String getName() {
            return this.nameSelector.getValue();
        }
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

        int y = this.y + 30 + params.size() * 15;

        if (MathHelper.is2dBetween(
                mouseX, mouseY,
                x + 3, y,
                x + font.width("+") + 1, y + 15
        )) {
            params.add(new ParamEntry(font));
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
