package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IfWidget extends BlockWidget {
    public static final MapCodec<IfWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i).and(
                    CodeWidget.CODEC.listOf().fieldOf("head").forGetter(w -> w.head)
            ).and(
                    BlockWidget.CODEC.optionalFieldOf("condition_body").forGetter(w -> Optional.ofNullable(w.conditionBody))
            ).and(
                    CodeWidget.CODEC.listOf().fieldOf("else_head").forGetter(w -> w.elseHead)
            ).and(
                    BlockWidget.CODEC.optionalFieldOf("else_body").forGetter(w -> Optional.ofNullable(w.elseBody))
            ).apply(i, IfWidget::new)
    );

    private final List<CodeWidget> head;
    private @Nullable BlockWidget conditionBody;
    private final List<CodeWidget> elseHead;
    private @Nullable BlockWidget elseBody;

    public IfWidget(List<CodeWidget> head, List<CodeWidget> elseHead) {
        this.head = head;
        this.elseHead = elseHead;
    }

    private IfWidget(BlockWidget child, List<CodeWidget> head, BlockWidget conditionBody, List<CodeWidget> elseHead, BlockWidget elseBody) {
        this(head, elseHead);
        this.conditionBody = conditionBody;
        this.elseBody = elseBody;
        this.setChild(child);
    }

    public IfWidget(Optional<BlockWidget> child, List<CodeWidget> headWidgets, Optional<BlockWidget> conditionBody, List<CodeWidget> elseWidgets, Optional<BlockWidget> elseBody) {
        child.ifPresent(this::setChild);
        this.head = headWidgets;
        this.conditionBody = conditionBody.orElse(null);
        this.elseHead = elseWidgets;
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
        if (y < getBranchHeight())
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
        RenderHelper.renderExprList(graphics, font, renderX + 6, renderY + 7, head);
        int bodyHeight = this.conditionBody != null ? this.conditionBody.getHeight() : 10;
        int headHeight = getHeadHeight();
        if (this.conditionBody != null)
            this.conditionBody.render(graphics, font, renderX + 6, renderY + headHeight);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + headHeight + 3, 6, bodyHeight - 3);
        if (elseHead != null) {
            graphics.blitSprite(CodeWidgetSprites.ELSE_CONDITION_HEAD, renderX, renderY + headHeight + bodyHeight, headWidth, 22);
            int elseHeadHeight = getHeadHeight();
            int elseBodyHeight = this.elseBody != null ? this.elseBody.getHeight() : 10;
            RenderHelper.renderExprList(graphics, font, renderX + 6, renderY + headHeight + bodyHeight + 7, elseHead);
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
        return Math.max(19, this.head.stream().mapToInt(CodeWidget::getHeight).max().orElse(19));
    }

    private int getElseHeadHeight() {
        return Math.max(19, this.head.stream().mapToInt(CodeWidget::getHeight).max().orElse(19));
    }

    private int getHeadWidth(Font font) {
        return this.head.stream().mapToInt(w -> w.getWidth(font)).sum();
    }

    private int getElseHeadWidth(Font font) {
        return this.elseHead.stream().mapToInt(w -> w.getWidth(font)).sum();
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }

    public static class Builder implements BlockWidget.Builder<IfWidget> {
        private final List<CodeWidget> head = new ArrayList<>(),
                elseHead = new ArrayList<>();
        private BlockWidget child, branch, elseBranch;
        public Builder headExpr(CodeWidget head) {
            this.head.add(head);
            return this;
        }

        public Builder elseHeadExpr(CodeWidget elseHead) {
            this.elseHead.add(elseHead);
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
            return this;
        }

        @Override
        public IfWidget build() {
            boolean hasElse = this.elseBranch != null || !this.elseHead.isEmpty();
            return new IfWidget(child, head, branch, hasElse ? elseHead : null, hasElse ? elseBranch : null);
        }
    }
}
