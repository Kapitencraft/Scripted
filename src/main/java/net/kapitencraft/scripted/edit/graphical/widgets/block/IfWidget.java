package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.ExprType;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.ParamWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IfWidget extends BlockWidget {
    public static final MapCodec<IfWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i).and(
                    CodeWidget.CODEC.fieldOf("head").forGetter(w -> w.condition)
            ).and(
                    BlockWidget.CODEC.optionalFieldOf("condition_body").forGetter(w -> Optional.ofNullable(w.conditionBody))
            ).and(
                    Codec.BOOL.optionalFieldOf("show_else", true).forGetter(w -> w.elseVisible)
            ).and(
                    BlockWidget.CODEC.optionalFieldOf("else_body").forGetter(w -> Optional.ofNullable(w.elseBody))
            ).apply(i, IfWidget::new)
    );

    private final CodeWidget condition;
    private boolean elseVisible;
    private @Nullable BlockWidget conditionBody;
    private @Nullable BlockWidget elseBody;

    public IfWidget(CodeWidget condition, List<CodeWidget> elseHead) {
        this.condition = condition;
    }

    private IfWidget(BlockWidget child, CodeWidget condition, BlockWidget conditionBody, List<CodeWidget> elseHead, BlockWidget elseBody, boolean showElse) {
        this(condition, elseHead);
        this.conditionBody = conditionBody;
        this.elseBody = elseBody;
        this.setChild(child);
        this.elseVisible = showElse;
    }

    public IfWidget(Optional<BlockWidget> child, CodeWidget headWidgets, Optional<BlockWidget> conditionBody, boolean elseVisible, Optional<BlockWidget> elseBody) {
        child.ifPresent(this::setChild);
        this.condition = headWidgets;
        this.elseVisible = elseVisible;
        this.conditionBody = conditionBody.orElse(null);
        this.elseBody = elseBody.orElse(null);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Type getType() {
        return Type.IF;
    }

    @Override
    public int getWidth(Font font) {
        return Math.max(6 + getHeadWidth(font), 6 + getElseHeadWidth(font));
    }

    @Override
    public int getHeight() {
        return getHeadHeight() +
                getElseHeadHeight() +
                (this.conditionBody != null ? this.conditionBody.getHeight() : 10) +
                (this.elseBody != null ? this.elseBody.getHeight() : 10);
    }

    @Override
    public BlockWidget getGhostBlockWidgetTarget(int x, int y) {
        if (y < getHeadHeight())
            return this;

        y -= getHeadHeight();
        if (y < getBranchHeight() && this.conditionBody != null)
            return this.conditionBody.getGhostBlockWidgetTarget(x, y);

        y -= getBranchHeight();
        if (y < getElseHeadHeight())
            return this;
        y -= getElseHeadHeight();
        if (y < 16)
            return this;
        return null;
    }

    private int getBranchHeight() {
        return this.conditionBody.getHeightWithChildren();
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int headWidth = getWidth(font);
        graphics.blitSprite(CodeWidgetSprites.LOOP_HEAD, renderX, renderY, headWidth, 22);
        RenderHelper.renderVisualText(graphics, font, renderX + 6, renderY + 7, "§if", Map.of("condition", condition));
        int bodyHeight = this.conditionBody != null ? this.conditionBody.getHeight() : 10;
        int headHeight = getHeadHeight();
        if (this.conditionBody != null)
            this.conditionBody.render(graphics, font, renderX + 6, renderY + headHeight);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + headHeight + 3, 6, bodyHeight - 3);
        if (elseVisible) {
            graphics.blitSprite(CodeWidgetSprites.ELSE_CONDITION_HEAD, renderX, renderY + headHeight + bodyHeight, headWidth, 22);
            int elseHeadHeight = getHeadHeight();
            int elseBodyHeight = this.elseBody != null ? this.elseBody.getHeight() : 10;
            RenderHelper.renderVisualText(graphics, font, renderX + 6, renderY + headHeight + bodyHeight + 7, "§else", Map.of());
            if (this.elseBody != null) {
                this.elseBody.render(graphics, font, renderX + 6, renderY + headHeight + bodyHeight + elseHeadHeight);
            }
            graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + headHeight + bodyHeight + elseHeadHeight + 3, 6, elseBodyHeight - 3);
            graphics.blitSprite(CodeWidgetSprites.SCOPE_END, renderX, renderY + headHeight + bodyHeight + elseHeadHeight + elseBodyHeight, headWidth, 16);
        } else {
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
        return null;
    }

    public static class Builder implements BlockWidget.Builder<IfWidget> {
        private CodeWidget head = new ParamWidget(ExprType.BOOLEAN);
        private boolean showElse = true;
        private final List<CodeWidget> elseHead = new ArrayList<>();
        private BlockWidget child, branch, elseBranch;
        public Builder setCondition(CodeWidget head) {
            this.head = head;
            return this;
        }

        public Builder elseHeadExpr(CodeWidget elseHead) {
            this.elseHead.add(elseHead);
            return this;
        }

        public Builder hideElse() {
            this.showElse = false;
            return this;
        }

        public Builder withChild(BlockWidget.Builder<?> builder) {
            this.child = builder.build();
            return this;
        }

        public Builder withBranch(BlockWidget.Builder<?> builder) {
            this.branch = builder.build();
            return this;
        }

        public Builder withElseBranch(BlockWidget.Builder<?> builder) {
            this.elseBranch = builder.build();
            this.showElse = true;
            return this;
        }

        @Override
        public IfWidget build() {
            boolean hasElse = this.elseBranch != null || !this.elseHead.isEmpty();
            return new IfWidget(child, head, branch, hasElse ? elseHead : null, hasElse ? elseBranch : null, showElse);
        }
    }
}
