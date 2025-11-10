package net.kapitencraft.scripted.edit.graphical.widgets;

import net.kapitencraft.kap_lib.helpers.MathHelper;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.IRenderable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class BodyWidget implements CodeWidget {
    private final List<CodeWidget> children;

    public BodyWidget(IRenderable renderable) {
        this.children = RenderHelper.decompileVisualText(renderable);
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY, int textX, int textY) {
        int i = 0;
        for (CodeWidget child : children) {
            child.render(graphics, font, renderX + i, renderY, textX + i, textY);
            i += child.getWidth(font);
        }
    }

    @Override
    public int getWidth(Font font) {
        return MathHelper.count(this.children.stream().map(w -> w.getWidth(font)).toList());
    }

    @Override
    public int getHeight() {
        return 0;
    }
}
