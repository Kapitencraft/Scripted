package net.kapitencraft.scripted.edit.graphical.widgets;

import jdk.dynalink.linker.LinkerServices;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class MethodWidget implements CodeWidget {
    private final List<CodeWidget> name, body;

    public MethodWidget(List<CodeWidget> name, List<CodeWidget> body) {
        this.name = name;
        this.body = body;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(CodeWidgetSprites.METHOD_HEAD, renderX, renderY, getWidth(font) + 5, 30);
        RenderHelper.renderExprList(graphics, font, renderX + 4, renderY + 15, this.name);
        RenderHelper.renderStmtList(graphics, font, renderX, renderY + 27, this.body);
    }

    @Override
    public int getWidth(Font font) {
        return CodeWidget.getWidthFromList(font, this.name);
    }

    @Override
    public int getHeight() {
        return 0;
    }
}
