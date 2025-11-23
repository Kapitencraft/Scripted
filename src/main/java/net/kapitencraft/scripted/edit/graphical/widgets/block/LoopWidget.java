package net.kapitencraft.scripted.edit.graphical.widgets.block;

import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LoopWidget extends BlockWidget {
    private final List<CodeWidget> head;
    private @Nullable BlockWidget body;

    public LoopWidget(List<CodeWidget> head, @Nullable BlockWidget body) {
        this.head = head;
        this.body = body;
    }

    private LoopWidget(BlockWidget child, List<CodeWidget> head, @Nullable BlockWidget body) {
        this.head = head;
        this.body = body;
        this.setChild(child);
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
        int bodyHeight = this.body != null ? this.body.getHeight() : 19;
        if (this.body != null)
            this.body.render(graphics, font, renderX + 6, renderY + getHeadHeight());
        graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + getHeadHeight() + 3, 6, bodyHeight - 3);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_END, renderX, renderY + getHeadHeight() + bodyHeight, loopWidth, 16);
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
        return getHeadHeight() + (this.body != null ? this.body.getHeight() : 19) + 13;
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < this.getHeadHeight()) {
            if (x < this.getWidth(font))
                return WidgetFetchResult.fromExprList(4, x, y, font, this, this.head);
            return null;
        }
        else if (y > this.getHeight()) {
            return this.fetchChildRemoveHovered(x, y - this.getHeight(), font);
        } else if (this.body != null) {
            WidgetFetchResult result = this.body.fetchAndRemoveHovered(x, y - this.getHeadHeight(), font);
            if (result == null) return null;
            if (!result.removed()) {
                this.body = null;
            }
            return result.setRemoved();
        }
        return WidgetFetchResult.notRemoved(this, x, y);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements BlockWidget.Builder<LoopWidget> {
        private BlockWidget child;
        private final List<CodeWidget> head = new ArrayList<>();
        private BlockWidget body;

        public Builder setBody(BlockWidget.Builder<?> widget) {
            this.body = widget.build();
            return this;
        }

        public Builder setChild(BlockWidget.Builder<?> widget) {
            this.child = widget.build();
            return this;
        }

        public Builder withHead(CodeWidget widget) {
            this.head.add(widget);
            return this;
        }

        @Override
        public LoopWidget build() {
            return new LoopWidget(child, head, body);
        }
    }
}
