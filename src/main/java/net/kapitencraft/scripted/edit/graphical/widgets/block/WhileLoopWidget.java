package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.kapitencraft.scripted.edit.graphical.inserter.block.ChildBlockGhostInserter;
import net.kapitencraft.scripted.edit.graphical.inserter.block.WhileBodyBlockGhostInserter;
import net.kapitencraft.scripted.edit.graphical.inserter.expr.ArgumentInserter;
import net.kapitencraft.scripted.edit.graphical.widgets.ArgumentStorage;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ParamWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class WhileLoopWidget extends BlockCodeWidget {
    public static final MapCodec<WhileLoopWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            BlockCodeWidget.commonFields(i).and(
                    ExprCodeWidget.CODEC.optionalFieldOf("condition", ParamWidget.CONDITION).forGetter(w -> w.condition)
            ).and(
                    BlockCodeWidget.CODEC.optionalFieldOf("body").forGetter(w -> Optional.ofNullable(w.body))
            ).apply(i, WhileLoopWidget::new)
    );

    @NotNull
    private ExprCodeWidget condition;
    private @Nullable BlockCodeWidget body;

    public WhileLoopWidget(@NotNull ExprCodeWidget condition, @Nullable BlockCodeWidget body) {
        this.condition = condition;
        this.body = body;
    }

    private WhileLoopWidget(BlockCodeWidget child, @NotNull ExprCodeWidget condition, @Nullable BlockCodeWidget body) {
        Preconditions.checkNotNull(condition);
        this.condition = condition;
        this.body = body;
        this.setChild(child);
    }

    public WhileLoopWidget(Optional<BlockCodeWidget> blockWidget, @NotNull ExprCodeWidget condition, Optional<BlockCodeWidget> body) {
        blockWidget.ifPresent(this::setChild);
        Preconditions.checkNotNull(condition);
        this.condition = condition;
        this.body = body.orElse(null);
    }

    @Override
    public BlockCodeWidget copy() {
        return new WhileLoopWidget(
                this.getChildCopy(),
                this.condition.copy(),
                this.body != null ? this.body.copy() : null
        );
    }

    @Override
    protected @NotNull Type getType() {
        return Type.WHILE_LOOP;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int loopWidth = 6 + getHeadWidth(font);
        int headHeight = getHeadHeight();
        graphics.blitSprite(CodeWidgetSprites.LOOP_HEAD, renderX, renderY, loopWidth, headHeight + 3);
        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 7 + (headHeight - 19) / 2, "§while", Map.of("condition", this.condition));
        int bodyHeight = getBranchHeight();
        if (this.body != null)
            this.body.render(graphics, font, renderX + 6, renderY + headHeight);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + headHeight + 3, 6, bodyHeight - 3);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_END, renderX, renderY + headHeight + bodyHeight, loopWidth, 16);
        super.render(graphics, font, renderX, renderY);
    }

    private int getHeadHeight() {
        return Math.max(18, this.condition.getHeight() + 4) + 2;
    }

    private int getBranchHeight() {
        return this.body != null ? this.body.getHeightWithChildren() : 10;
    }

    private int getHeadWidth(Font font) {
        return RenderHelper.getVisualTextWidth(font, "§while", Map.of("condition", this.condition));
    }

    @Override
    public int getWidth(Font font) {
        int width = 6 + getHeadWidth(font);
        if (this.body != null) {
            int i = this.body.getWidth(font) + 6;
            if (i > width)
                width = i;
        }
        return width;
    }

    @Override
    public int getHeight() {
        return getHeadHeight() +
                getBranchHeight() + 13;
    }

    public void setBody(@Nullable BlockCodeWidget target) {
        this.body = target;
    }

    public void insertBodyMiddle(BlockCodeWidget widget) {
        widget.setChild(this.body);
        this.body = widget;
    }

    @Override
    public GhostInserter getGhostWidgetTarget(int x, int y, Font font, boolean isBlock) {
        if (y < 0) return null;
        if (!isBlock && y < this.getHeadHeight()) {
            return ArgumentInserter.create(x, y, font, "§while", (s, widget) -> {
                if (!"condition".equals(s))
                    throw new IllegalArgumentException("unknown while argument: " + s);
                this.setCondition(widget);
            }, Map.of("condition", this.condition));
        }
        y -= this.getHeadHeight();
        if (isBlock && y < 10 && x > -10 && x < 40)
            return new WhileBodyBlockGhostInserter(this);
        if (this.body != null && y < this.body.getHeightWithChildren())
            return this.body.getGhostWidgetTarget(x - 6, y, font, isBlock);
        y -= this.body != null ? this.body.getHeightWithChildren() : 10;
        y -= 16;
        if (isBlock && y < 10 && x > -10 && x < 40)
            return new ChildBlockGhostInserter(this);
        if (this.getChild() != null)
            return this.getChild().getGhostWidgetTarget(x, y, font, isBlock);
        return null;
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < this.getHeadHeight()) {
            return WidgetFetchResult.fromExprList(4, x, y, font, this, "§while", ArgumentStorage.createSingle("condition", this::setCondition, () -> this.condition));
        } else if (y > this.getHeight()) {
            return this.fetchChildRemoveHovered(x, y - this.getHeight(), font);
        } else if (this.body != null) {
            WidgetFetchResult result = this.body.fetchAndRemoveHovered(x, y - this.getHeadHeight(), font);
            if (result == null) return null;
            if (!result.removed()) {
                this.body = null;
            }
            return result.setRemoved();
        }
        return BlockWidgetFetchResult.notRemoved(this, x, y);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setCondition(@Nullable ExprCodeWidget target) {
        this.condition = target == null ? ParamWidget.CONDITION : target;
    }

    public static class Builder implements BlockCodeWidget.Builder<WhileLoopWidget> {
        private BlockCodeWidget child;
        private ExprCodeWidget condition = ParamWidget.CONDITION;
        private BlockCodeWidget body;

        public Builder setBody(BlockCodeWidget.Builder<?> widget) {
            this.body = widget.build();
            return this;
        }

        public Builder setChild(BlockCodeWidget.Builder<?> widget) {
            this.child = widget.build();
            return this;
        }

        public Builder setCondition(ExprCodeWidget widget) {
            this.condition = widget;
            return this;
        }

        public Builder setCondition(ExprCodeWidget.Builder<?> builder) {
            this.condition = builder.build();
            return this;
        }

        @Override
        public WhileLoopWidget build() {
            return new WhileLoopWidget(child, condition, body);
        }
    }
}
