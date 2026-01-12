package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.kapitencraft.scripted.edit.graphical.inserter.block.ChildBlockGhostInserter;
import net.kapitencraft.scripted.edit.graphical.inserter.block.IfBodyBlockGhostInserter;
import net.kapitencraft.scripted.edit.graphical.inserter.block.IfElseBodyBlockGhostInserter;
import net.kapitencraft.scripted.edit.graphical.inserter.expr.ArgumentInserter;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ParamWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class IfWidget extends BlockCodeWidget {
    public static final MapCodec<IfWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i).and(
                    ExprCodeWidget.CODEC.optionalFieldOf("condition", ParamWidget.CONDITION).forGetter(w -> w.condition)
            ).and(
                    BlockCodeWidget.CODEC.optionalFieldOf("condition_body").forGetter(w -> Optional.ofNullable(w.conditionBody))
            ).and(
                    Codec.BOOL.optionalFieldOf("show_else", true).forGetter(w -> w.elseVisible)
            ).and(
                    ExprCodeWidget.CODEC.optionalFieldOf("else_condition", ParamWidget.CONDITION).forGetter(w -> w.elseCondition)
            ).and(
                    BlockCodeWidget.CODEC.optionalFieldOf("else_body").forGetter(w -> Optional.ofNullable(w.elseBody))
            ).apply(i, IfWidget::new)
    );

    private ExprCodeWidget condition;
    private boolean elseVisible;
    private @Nullable BlockCodeWidget conditionBody;
    private ExprCodeWidget elseCondition;
    private @Nullable BlockCodeWidget elseBody;

    public IfWidget(ExprCodeWidget condition, ExprCodeWidget elseCondition) {
        this.condition = condition;
        this.elseCondition = elseCondition;
    }

    private IfWidget(BlockCodeWidget child, ExprCodeWidget condition, @Nullable BlockCodeWidget conditionBody, ExprCodeWidget elseCondition, @Nullable BlockCodeWidget elseBody, boolean showElse) {
        this(condition, elseCondition);
        this.conditionBody = conditionBody;
        this.elseBody = elseBody;
        this.setChild(child);
        this.elseVisible = showElse;
    }

    public IfWidget(Optional<BlockCodeWidget> child, ExprCodeWidget headWidgets, Optional<BlockCodeWidget> conditionBody, boolean elseVisible, ExprCodeWidget elseCondition, Optional<BlockCodeWidget> elseBody) {
        this.elseCondition = elseCondition;
        child.ifPresent(this::setChild);
        this.condition = headWidgets;
        this.elseVisible = elseVisible;
        this.conditionBody = conditionBody.orElse(null);
        this.elseBody = elseBody.orElse(null);
    }

    @Override
    public BlockCodeWidget copy() {
        return new IfWidget(
                this.getChildCopy(),
                this.condition.copy(),
                this.conditionBody != null ? this.conditionBody.copy() : null,
                this.elseCondition.copy(),
                this.elseBody != null ? this.elseBody.copy() : null,
                this.elseVisible
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public @NotNull Type getType() {
        return Type.IF;
    }

    @Override
    public int getWidth(Font font) {
        return Math.max(6 + getHeadWidth(font), 6 + getElseHeadWidth(font));
    }

    @Override
    public int getHeight() {
        return getHeadHeight() +
                this.getBranchHeight() +
                (this.elseVisible ? this.getElseHeadHeight() +
                        (this.elseBody != null ? this.elseBody.getHeight() : 10) : 0) +
                13;
    }

    @Override
    public GhostInserter getGhostWidgetTarget(int x, int y, Font font) {
        if (y < 0) return null;
        if (y < getHeadHeight()) {
            return ArgumentInserter.createSpecific(x, y, font, "§if", "condition", this::setCondition, this.condition);
        }
        y -= getHeadHeight();
        if (y < 10 && x > -10 && x < 30)
            return new IfBodyBlockGhostInserter(this);

        if (y < getBranchHeight()) {
            if (this.conditionBody != null)
                return this.conditionBody.getGhostWidgetTarget(x, y, font);
            if (x > -4 && x < 36 && y < 15)
                return new IfBodyBlockGhostInserter(this);
        }
        y -= this.getBranchHeight();

        if (elseVisible) {
            y -= getBranchHeight();
            if (x > -4 && x < 36 && y < getElseHeadHeight())
                return new IfElseBodyBlockGhostInserter(this);
            y -= getElseHeadHeight();
            if (x > -10 && x < 30 && y < 16)
                return new ChildBlockGhostInserter(this);
        }
        if (y < 23 && x > -10 && x < 30) {
            return new ChildBlockGhostInserter(this);
        }
        return null;
    }

    private int getBranchHeight() {
        return this.conditionBody == null ? 10 : this.conditionBody.getHeightWithChildren();
    }

    private int getElseBranchHeight() {
        return this.elseBody == null ? 10 : this.elseBody.getHeightWithChildren();
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int headWidth = getWidth(font);
        //head
        graphics.blitSprite(CodeWidgetSprites.LOOP_HEAD, renderX, renderY, headWidth, 22);
        RenderHelper.renderVisualText(graphics, font, renderX + 6, renderY + 7, "§if", Map.of("condition", condition));

        //body
        int bodyHeight = this.conditionBody != null ? this.conditionBody.getHeightWithChildren() : 10;
        int headHeight = getHeadHeight();
        if (this.conditionBody != null)
            this.conditionBody.render(graphics, font, renderX + 6, renderY + headHeight);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + headHeight + 3, 6, bodyHeight - 3);

        if (elseVisible) {
            //else
            graphics.blitSprite(CodeWidgetSprites.ELSE_CONDITION_HEAD, renderX, renderY + headHeight + bodyHeight, headWidth, 22);
            int elseHeadHeight = getHeadHeight();
            int elseBodyHeight = this.elseBody != null ? this.elseBody.getHeight() : 10;
            RenderHelper.renderVisualText(graphics, font, renderX + 6, renderY + headHeight + bodyHeight + 7, "§else", Map.of());
            if (this.elseBody != null) {
                this.elseBody.render(graphics, font, renderX + 6, renderY + headHeight + bodyHeight + elseHeadHeight);
            }
            graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + headHeight + bodyHeight + elseHeadHeight + 3, 6, elseBodyHeight - 3);
            //end
            graphics.blitSprite(CodeWidgetSprites.SCOPE_END, renderX, renderY + headHeight + bodyHeight + elseHeadHeight + elseBodyHeight, headWidth, 16);
        } else {
            //end
            graphics.blitSprite(CodeWidgetSprites.SCOPE_END, renderX, renderY + headHeight + bodyHeight, headWidth, 16);
        }
        super.render(graphics, font, renderX, renderY);
    }

    private int getHeadHeight() {
        return Math.max(19, this.condition.getHeight());
    }

    private int getElseHeadHeight() {
        return Math.max(19, this.condition.getHeight());
    }

    private int getHeadWidth(Font font) {
        return RenderHelper.getVisualTextWidth(font, "§if", Map.of("condition", condition));
    }

    private int getElseHeadWidth(Font font) {
        return RenderHelper.getVisualTextWidth(font, "§else", Map.of());
    }


    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < this.getHeadHeight()) {
            return BlockWidgetFetchResult.notRemoved(this, x, y);
        }

        if (y - this.getHeadHeight() < getBranchHeight()) {
            if (x < 6)
                return BlockWidgetFetchResult.notRemoved(this, x, y);
            if (this.conditionBody != null) {
                WidgetFetchResult result = this.conditionBody.fetchAndRemoveHovered(x - 6, y - this.getHeadHeight(), font);
                if (result == null) return null;
                if (!result.removed())
                    this.conditionBody = null;
                return result.setRemoved();
            }
            return null;
        }
        if (elseVisible) {
            if (y - this.getHeadHeight() - this.getBranchHeight() < this.getElseHeadHeight()) {
                if (x < this.getElseHeadWidth(font))
                    return BlockWidgetFetchResult.notRemoved(this, x, y);
                return null;
            }
            if (y - this.getHeadHeight() - this.getBranchHeight() - this.getElseHeadHeight() < this.getElseBranchHeight()) {
                if (x < 6)
                    return BlockWidgetFetchResult.notRemoved(this, x, y);
                if (elseBody != null) {
                    WidgetFetchResult result = this.elseBody.fetchAndRemoveHovered(x - 6,
                            y - this.getHeadHeight() - this.getBranchHeight() - this.getElseHeadHeight(), font);
                    if (result == null) return null;
                    if (!result.removed())
                        this.elseBody = null;
                    return result.setRemoved();
                }
                return null;
            }
        }
        if (y > this.getHeight())
            return this.fetchChildRemoveHovered(x, y - getHeight(), font);
        return null;
    }

    public void setBody(@Nullable BlockCodeWidget conditionBody) {
        this.conditionBody = conditionBody;
    }

    public void setElseBody(@Nullable BlockCodeWidget elseBody) {
        this.elseBody = elseBody;
    }

    public void insertBodyMiddle(BlockCodeWidget widget) {
        widget.setChild(this.conditionBody);
        this.conditionBody = widget;
    }

    public void insertElseMiddle(BlockCodeWidget widget) {
        widget.setChild(this.elseBody);
        this.elseBody = widget;
    }

    public void setCondition(@Nullable ExprCodeWidget target) {
        this.condition = target == null ? ParamWidget.CONDITION : target;
    }

    public void setElseCondition(ExprCodeWidget target) {
        this.elseCondition = target;
    }

    public static class Builder implements BlockCodeWidget.Builder<IfWidget> {
        private ExprCodeWidget condition = ParamWidget.CONDITION,
                elseCondition = ParamWidget.CONDITION;
        private boolean showElse = true;
        private BlockCodeWidget child, branch, elseBranch;

        public Builder setCondition(ExprCodeWidget head) {
            this.condition = head;
            return this;
        }

        public Builder setCondition(ExprCodeWidget.Builder<?> builder) {
            return this.setCondition(builder.build());
        }

        public Builder hideElse() {
            this.showElse = false;
            return this;
        }

        public Builder withChild(BlockCodeWidget.Builder<?> builder) {
            this.child = builder.build();
            return this;
        }

        public Builder withBranch(BlockCodeWidget.Builder<?> builder) {
            this.branch = builder.build();
            return this;
        }

        public Builder withElseBranch(BlockCodeWidget.Builder<?> builder) {
            this.elseBranch = builder.build();
            this.showElse = true;
            return this;
        }

        public Builder setElseCondition(ExprCodeWidget.Builder<?> builder) {
            this.elseCondition = builder.build();
            this.showElse = true;
            return this;
        }

        @Override
        public IfWidget build() {
            return new IfWidget(child, condition, branch, elseCondition, showElse ? elseBranch : null, showElse);
        }
    }
}
