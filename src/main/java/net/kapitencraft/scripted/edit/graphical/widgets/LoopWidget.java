package net.kapitencraft.scripted.edit.graphical.widgets;

import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class LoopWidget implements CodeWidget {
    private final List<CodeWidget> head;
    private final ScopeWidget body;

    public LoopWidget(List<CodeWidget> head, ScopeWidget body) {
        this.head = head;
        this.body = body;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int loopWidth = 6 + getHeadWidth(font);
        graphics.blitSprite(CodeWidgetSprites.LOOP_HEAD, renderX, renderY, loopWidth, 22);
        RenderHelper.renderExprList(graphics, font, renderX + 4, renderY + 7, head);
        this.body.render(graphics, font, renderX + 6, renderY + getHeadHeight());
        graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + getHeadHeight() + 3, 6, this.body.getHeight() - 3);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_END, renderX, renderY + getHeadHeight() + this.body.getHeight(), loopWidth, 16);
    }

    private int getHeadHeight() {
        return Math.max(19, this.head.stream().mapToInt(CodeWidget::getHeight).max().orElse(19));
    }

    private int getHeadWidth(Font font) {
        return this.head.stream().mapToInt(w -> w.getWidth(font)).sum();
    }

    @Override
    public int getWidth(Font font) {
        return getHeadWidth(font);
    }

    @Override
    public int getHeight() {
        return getHeadHeight() + this.body.getHeight() + 13;
    }
}
