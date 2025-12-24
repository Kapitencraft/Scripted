package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.ghost.ChildGhostInserter;
import net.kapitencraft.scripted.edit.graphical.ghost.GhostInserter;
import net.kapitencraft.scripted.edit.graphical.ghost.WhileBodyGhostInserter;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.ParamWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class WhileLoopWidget extends BlockWidget {
    public static final MapCodec<WhileLoopWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            BlockWidget.commonFields(i).and(
                    CodeWidget.CODEC.optionalFieldOf("condition", ParamWidget.CONDITION).forGetter(w -> w.condition)
            ).and(
                    BlockWidget.CODEC.optionalFieldOf("body").forGetter(w -> Optional.ofNullable(w.body))
            ).apply(i, WhileLoopWidget::new)
    );

    private final CodeWidget condition;
    private @Nullable BlockWidget body;

    public WhileLoopWidget(CodeWidget condition, @Nullable BlockWidget body) {
        this.condition = condition;
        this.body = body;
    }

    private WhileLoopWidget(BlockWidget child, CodeWidget head, @Nullable BlockWidget body) {
        this.condition = head;
        this.body = body;
        this.setChild(child);
    }

    public WhileLoopWidget(Optional<BlockWidget> blockWidget, CodeWidget widgets, Optional<BlockWidget> body) {
        blockWidget.ifPresent(this::setChild);
        this.condition = widgets;
        this.body = body.orElse(null);
    }

    @Override
    public BlockWidget copy() {
        return new WhileLoopWidget(
                this.getChildCopy(),
                this.condition.copy(),
                this.body != null ? this.body.copy() : null
        );
    }

    @Override
    public @NotNull Type getType() {
        return Type.WHILE_LOOP;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int loopWidth = 6 + getHeadWidth(font);
        graphics.blitSprite(CodeWidgetSprites.LOOP_HEAD, renderX, renderY, loopWidth, 22);
        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 7, "§while", Map.of("condition", this.condition));
        int bodyHeight = this.body != null ? this.body.getHeight() : 10;
        if (this.body != null)
            this.body.render(graphics, font, renderX + 6, renderY + getHeadHeight());
        graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + getHeadHeight() + 3, 6, bodyHeight - 3);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_END, renderX, renderY + getHeadHeight() + bodyHeight, loopWidth, 16);
        super.render(graphics, font, renderX, renderY);
    }

    private int getHeadHeight() {
        return Math.max(19, this.condition.getHeight());
    }

    private int getHeadWidth(Font font) {
        return RenderHelper.getVisualTextWidth(font, "§while", Map.of("condition", this.condition));
    }

    @Override
    public int getWidth(Font font) {
        return getHeadWidth(font);
    }

    @Override
    public int getHeight() {
        return getHeadHeight() + (this.body != null ? this.body.getHeight() : 19) + 13;
    }

    public void setBody(BlockWidget target) {
        this.body = target;
    }

    public void insertBodyMiddle(BlockWidget widget) {
        widget.setChild(this.body);
        this.body = widget;
    }

    @Override
    public GhostInserter getGhostBlockWidgetTarget(int x, int y) {
        if (y < 0) return null;
        if (y < this.getHeadHeight() + 10 && x > -10 && x < 40)
            return new WhileBodyGhostInserter(this);
        y -= this.getHeadHeight();
        if (this.body != null && y < this.body.getHeightWithChildren())
            return this.body.getGhostBlockWidgetTarget(x, y);
        if (y < 16)
            return new ChildGhostInserter(this);
        y -= 16;
        if (this.getChild() != null)
            return this.getChild().getGhostBlockWidgetTarget(x, y);
        return null;
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < this.getHeadHeight()) {
            if (x < this.getWidth(font))
                return WidgetFetchResult.fromExprList(4, x, y, font, this, "§while", Map.of("condition", this.condition));
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

    public static class Builder implements BlockWidget.Builder<WhileLoopWidget> {
        private BlockWidget child;
        private CodeWidget condition = ParamWidget.CONDITION;
        private BlockWidget body;

        public Builder setBody(BlockWidget.Builder<?> widget) {
            this.body = widget.build();
            return this;
        }

        public Builder setChild(BlockWidget.Builder<?> widget) {
            this.child = widget.build();
            return this;
        }

        public Builder setCondition(CodeWidget widget) {
            this.condition = widget;
            return this;
        }

        public Builder setCondition(CodeWidget.Builder<?> builder) {
            this.condition = builder.build();
            return this;
        }

        @Override
        public WhileLoopWidget build() {
            return new WhileLoopWidget(child, condition, body);
        }
    }
}
