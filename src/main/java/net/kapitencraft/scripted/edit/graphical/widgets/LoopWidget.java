package net.kapitencraft.scripted.edit.graphical.widgets;

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
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY, int textX, int textY) {

        graphics.blitSprite(CodeWidgetSprites.LOOP_HEAD, 100, 138, 6 + getWidth(font), 22);
        int i = 0;
        for (CodeWidget child : head) {
            child.render(graphics, font, renderX + i, renderY, textX + i, textY);
            i += child.getWidth(font);
        }

    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }
}
