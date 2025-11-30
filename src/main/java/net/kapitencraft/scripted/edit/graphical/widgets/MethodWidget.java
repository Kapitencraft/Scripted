package net.kapitencraft.scripted.edit.graphical.widgets;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class MethodWidget implements CodeWidget {
    public static final MapCodec<MethodWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            CodeWidget.CODEC.listOf().fieldOf("name").forGetter(w -> w.name),
            CodeWidget.CODEC.listOf().fieldOf("body").forGetter(w -> w.body)
    ).apply(i, MethodWidget::new));

    private final List<CodeWidget> name, body;

    public MethodWidget(List<CodeWidget> name, List<CodeWidget> body) {
        this.name = name;
        this.body = body;
    }

    @Override
    public Type getType() {
        return Type.METHOD;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(CodeWidgetSprites.METHOD_HEAD, renderX, renderY, getWidth(font) + 5, 30);
        RenderHelper.renderExprList(graphics, font, renderX + 4, renderY + 15, this.name);
        RenderHelper.renderExprList(graphics, font, renderX, renderY + 27, this.body);
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
