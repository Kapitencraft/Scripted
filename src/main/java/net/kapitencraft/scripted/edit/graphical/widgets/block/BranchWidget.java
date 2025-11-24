package net.kapitencraft.scripted.edit.graphical.widgets.block;

import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BranchWidget extends BlockWidget {
    private final List<CodeWidget> head;
    private BlockWidget conditionBody;
    private final List<CodeWidget> elseHead;
    private BlockWidget elseBody;

    public BranchWidget(List<CodeWidget> head, List<CodeWidget> elseHead) {
        this.head = head;
        this.elseHead = elseHead;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int headWidth = 6 + getHeadWidth(font);
        graphics.blitSprite(CodeWidgetSprites.LOOP_HEAD, renderX, renderY, headWidth, 22);
        RenderHelper.renderExprList(graphics, font, renderX, renderY, head);
        int bodyHeight = this.conditionBody != null ? this.conditionBody.getHeight() : 10;
        int headHeight = getHeadHeight();
        if (this.conditionBody != null)
            this.conditionBody.render(graphics, font, renderX + 6, renderY + headHeight);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + headHeight + 3, 6, bodyHeight - 3);
        if (elseHead != null) {
            graphics.blitSprite(CodeWidgetSprites.ELSE_CONDITION_HEAD, renderX, renderY + headHeight + bodyHeight, headWidth, 22);
            int elseHeadHeight = 6 + getHeadHeight();
            if (this.elseBody != null) {
                this.elseBody.render(graphics, font, renderX + 6, renderY + headHeight + bodyHeight + elseHeadHeight);
            }
        }
        graphics.blitSprite(CodeWidgetSprites.SCOPE_END, renderX, renderY + headHeight + bodyHeight, headWidth, 16);
        super.render(graphics, font, renderX, renderY);
    }

    private int getHeadHeight() {
        return Math.max(19, this.head.stream().mapToInt(CodeWidget::getHeight).max().orElse(19));
    }

    private int getElseHeadHeight() {
        return Math.max(19, this.head.stream().mapToInt(CodeWidget::getHeight).max().orElse(19));
    }

    private int getHeadWidth(Font font) {
        return this.head.stream().mapToInt(w -> w.getWidth(font)).sum();
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }
}
