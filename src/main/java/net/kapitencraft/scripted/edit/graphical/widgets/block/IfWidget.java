package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.CommonBranchBlockConnector;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.connector.SingletonExprConnector;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.ArgumentStorage;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ParamWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.InteractionData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class IfWidget extends BlockCodeWidget {
    public static final MapCodec<IfWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i).and(
                    ExprCodeWidget.CODEC.optionalFieldOf("condition", ParamWidget.CONDITION).forGetter(w -> w.condition)
            ).and(
                    BlockCodeWidget.CODEC.optionalFieldOf("condition_body").forGetter(w -> Optional.ofNullable(w.conditionBody))
            ).and(
                    Codec.BOOL.optionalFieldOf("show_else", true).forGetter(w -> w.elseVisible)
            ).and(
                    BlockCodeWidget.CODEC.optionalFieldOf("else_body").forGetter(w -> Optional.ofNullable(w.elseBody))
            ).apply(i, IfWidget::new)
    );

    private ExprCodeWidget condition;
    private boolean elseVisible;
    private @Nullable BlockCodeWidget conditionBody;
    private @Nullable BlockCodeWidget elseBody;

    public IfWidget(ExprCodeWidget condition) {
        this.condition = condition;
    }

    private IfWidget(BlockCodeWidget child, ExprCodeWidget condition, @Nullable BlockCodeWidget conditionBody, @Nullable BlockCodeWidget elseBody, boolean showElse) {
        this(condition);
        this.conditionBody = conditionBody;
        this.elseBody = elseBody;
        this.setChild(child);
        this.elseVisible = showElse;
    }

    public IfWidget(Optional<BlockCodeWidget> child, ExprCodeWidget headWidgets, Optional<BlockCodeWidget> conditionBody, boolean elseVisible, Optional<BlockCodeWidget> elseBody) {
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
                this.elseBody != null ? this.elseBody.copy() : null,
                this.elseVisible
        );
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        if (arg.equals("condition")) {
            this.condition = obj;
        }
        //TODO add else-ifs
    }

    @Override
    public CodeWidget getByName(String argName) {
        if ("condition".equals(argName)) {
            return this.condition;
        }
        throw new IllegalArgumentException("unknown argument named " + argName + " in If");
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected @NotNull Type getType() {
        return Type.IF;
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        collector.accept(new SingletonExprConnector(
                aX + 4 + RenderHelper.getPartialWidth(font, "§if", Map.of(), "condition"),
                aY,
                this::setCondition,
                () -> this.condition
        ));

        int headHeight = this.getHeadHeight();
        collector.accept(new CommonBranchBlockConnector(
                aX + 6,
                aY + headHeight,
                this::setBody,
                () -> this.conditionBody,
                font,
                collector
        ));
        if (this.elseVisible) {
            collector.accept(new CommonBranchBlockConnector(
                    aX + 6,
                    aY + headHeight + this.getBodyHeight() + this.getElseHeadHeight(),
                    this::setElseBody,
                    () -> this.elseBody,
                    font,
                    collector
            ));
        }
        super.collectConnectors(aX, aY, font, collector);
    }

    //region size
    @Override
    public int getWidth(Font font) {
        int width = getHeadWidth(font);
        int elseHeadWidth = getElseHeadWidth(font);
        if (elseHeadWidth > width) width = elseHeadWidth;
        if (this.conditionBody != null) {
            int i = this.conditionBody.getWidth(font) + 6;
            if (i > width)
                width = i;
        }
        if (this.elseBody != null) {
            int i = this.elseBody.getWidth(font) + 6;
            if (i > width)
                width = i;
        }
        return width;
    }

    @Override
    public int getHeight() {
        return getHeadHeight() +
                this.getBodyHeight() +
                (this.elseVisible ? this.getElseHeadHeight() + getElseBodyHeight() : 0) +
                13; //height of the bottom enclose part - 3 for the offset
    }

    private int getBodyHeight() {
        return this.conditionBody == null ? 10 : this.conditionBody.getHeightWithChildren();
    }

    private int getElseBodyHeight() {
        return this.elseBody == null ? 10 : this.elseBody.getHeightWithChildren();
    }

    private int getHeadHeight() {
        return Math.max(18, this.condition.getHeight() + 4) + 2;
    }

    private int getElseHeadHeight() {
        return 18;
    }

    private int getHeadWidth(Font font) {
        return 4 + RenderHelper.getVisualTextWidth(font, "§if", Map.of("condition", condition));
    }

    private int getElseHeadWidth(Font font) {
        return RenderHelper.getVisualTextWidth(font, "§else", Map.of());
    }
    //endregion

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int headWidth = getHeadWidth(font);
        int headHeight = getHeadHeight();
        //head
        graphics.blitSprite(CodeWidgetSprites.LOOP_HEAD, renderX, renderY, headWidth, headHeight + 3);
        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 7 + (headHeight - 18) / 2, "§if", Map.of("condition", condition));

        //body
        int bodyHeight = getBodyHeight();
        if (this.conditionBody != null)
            this.conditionBody.render(graphics, font, renderX + 6, renderY + headHeight);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + headHeight + 3, 6, bodyHeight - 3);

        if (elseVisible) {
            //else
            int elseHeadHeight = getElseHeadHeight();
            graphics.blitSprite(CodeWidgetSprites.ELSE_CONDITION_HEAD, renderX, renderY + headHeight + bodyHeight, headWidth, elseHeadHeight + 3);
            int elseBodyHeight = getElseBodyHeight();
            RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + headHeight + bodyHeight + 7, "§else", Map.of());
            if (this.elseBody != null) {
                this.elseBody.render(graphics, font, renderX + 6, renderY + headHeight + bodyHeight + elseHeadHeight);
            }
            graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + headHeight + bodyHeight + elseHeadHeight + 3, 6, elseBodyHeight - 3);
            //end
            renderScopeEnd(graphics, renderX, renderY + headHeight + bodyHeight + elseHeadHeight + elseBodyHeight, headWidth);
        } else {
            //end
            renderScopeEnd(graphics, renderX, renderY + headHeight + bodyHeight, headWidth);
        }
        super.render(graphics, font, renderX, renderY);
    }

    private void renderScopeEnd(GuiGraphics graphics, int renderX, int renderY, int width) {
        graphics.blitSprite(CodeWidgetSprites.SCOPE_END, renderX, renderY, width, 16);
        graphics.blitSprite(CodeWidgetSprites.MODIFY_IF, renderX + width - 9, renderY + 4, 7, 7);
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < this.getHeadHeight()) {
            if (x < this.getHeadWidth(font))
                return BlockWidgetFetchResult.fromExprList(4, x, y, font, this, "§if", ArgumentStorage.createSingle("condition", this::setCondition, () -> this.condition));
            return null;
        }

        if (y - this.getHeadHeight() < getBodyHeight()) {
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
            if (y - this.getHeadHeight() - this.getBodyHeight() < this.getElseHeadHeight()) {
                if (x < this.getElseHeadWidth(font))
                    return BlockWidgetFetchResult.notRemoved(this, x, y);
                return null;
            }
            if (y - this.getHeadHeight() - this.getBodyHeight() - this.getElseHeadHeight() < this.getElseBodyHeight()) {
                if (x < 6)
                    return BlockWidgetFetchResult.notRemoved(this, x, y);
                if (elseBody != null) {
                    WidgetFetchResult result = this.elseBody.fetchAndRemoveHovered(x - 6,
                            y - this.getHeadHeight() - this.getBodyHeight() - this.getElseHeadHeight(), font);
                    if (result == null) return null;
                    if (!result.removed())
                        this.elseBody = null;
                    return result.setRemoved();
                }
                return null;
            }
        }
        if (y > this.getHeight())
            return super.fetchAndRemoveHovered(x, y - getHeight(), font);
        return null;
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        //TODO
        this.condition.registerInteractions(xOrigin, yOrigin, font, sink);
        if (elseVisible) {
            int width = getHeadWidth(font);
            int height = getHeadHeight() + getBodyHeight() + getElseHeadHeight() + getElseBodyHeight();
            sink.accept(new ModifyWidgetBranchesInteraction(xOrigin + width - 9, yOrigin + height, 7, 7));
        }
    }

    private class ModifyWidgetBranchesInteraction extends CodeInteraction {

        protected ModifyWidgetBranchesInteraction(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void onClick(int mouseX, int mouseY, InteractionData callbacks) {

        }
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

    @Override
    public void update(@Nullable MethodContext context) {
        this.condition.update(context);
        if (this.conditionBody != null) {
            if (context != null)
                context.lvt.push();
            this.conditionBody.update(context);
            if (context != null) {
                context.lvt.pop();
            }
        }
        if (this.elseVisible) {
            if (this.elseBody != null) {
                if (context != null)
                    context.lvt.push();
                this.elseBody.update(context);
                if (context != null) {
                    context.lvt.pop();
                }
            }
        }
        super.update(context);
    }

    public static class Builder implements BlockCodeWidget.Builder<IfWidget> {
        private ExprCodeWidget condition = ParamWidget.CONDITION;
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

        @Override
        public IfWidget build() {
            return new IfWidget(child, condition, branch, showElse ? elseBranch : null, showElse);
        }
    }
}
