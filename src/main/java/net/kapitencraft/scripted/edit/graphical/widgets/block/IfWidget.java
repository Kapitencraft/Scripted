package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.kap_lib.core.client.widget.PositionedWidget;
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

import java.util.ArrayList;
import java.util.List;
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
            ).and(
                    ElseIfEntry.CODEC.listOf().fieldOf("elifs").forGetter(w -> w.elseIfs)
            ).apply(i, IfWidget::new)
    );

    private ExprCodeWidget condition;
    private @Nullable BlockCodeWidget conditionBody;
    private boolean elseVisible;
    private @Nullable BlockCodeWidget elseBody;
    private final List<ElseIfEntry> elseIfs = new ArrayList<>();

    private int globalHeadWidth;

    public IfWidget(ExprCodeWidget condition) {
        this.condition = condition;
    }

    private IfWidget(BlockCodeWidget child, ExprCodeWidget condition, @Nullable BlockCodeWidget conditionBody, @Nullable BlockCodeWidget elseBody, boolean showElse, List<ElseIfEntry> elifs) {
        this(condition);
        this.conditionBody = conditionBody;
        this.elseBody = elseBody;
        this.setChild(child);
        this.elseVisible = showElse;
        this.elseIfs.addAll(elifs);
    }

    public IfWidget(Optional<BlockCodeWidget> child, ExprCodeWidget headWidgets, Optional<BlockCodeWidget> conditionBody, boolean elseVisible, Optional<BlockCodeWidget> elseBody, List<ElseIfEntry> elseIfs) {
        child.ifPresent(this::setChild);
        this.condition = headWidgets;
        this.elseVisible = elseVisible;
        this.conditionBody = conditionBody.orElse(null);
        this.elseBody = elseBody.orElse(null);
        this.elseIfs.addAll(elseIfs);
    }

    public boolean isElseVisible() {
        return elseVisible;
    }

    @Override
    public BlockCodeWidget copy() {
        return new IfWidget(
                this.getChildCopy(),
                this.condition.copy(),
                this.conditionBody != null ? this.conditionBody.copy() : null,
                this.elseBody != null ? this.elseBody.copy() : null,
                this.elseVisible,
                copyElifs(this.elseIfs)
        );
    }

    private List<ElseIfEntry> copyElifs(List<ElseIfEntry> elseIfs) {
        return elseIfs.stream().map(ElseIfEntry::copy).toList();
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        if (arg.equals("condition")) {
            this.condition = obj;
        }
        if (arg.startsWith("elif-condition")) {
            int idx = Integer.parseInt(arg.substring(14));
            this.elseIfs.get(idx).condition = obj;
        }
        throw new IllegalArgumentException("unknown argument in if widget: " + arg);
    }

    @Override
    public CodeWidget getByName(String argName) {
        if ("condition".equals(argName)) {
            return this.condition;
        }
        if (argName.startsWith("elif-condition")) {
            int idx = Integer.parseInt(argName.substring(14));
            return this.elseIfs.get(idx).condition;
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
        int conditionOffset = aX + 4 + RenderHelper.getPartialWidth(font, "§if", Map.of(), "condition");
        collector.accept(new SingletonExprConnector(
                conditionOffset,
                aY,
                this::setCondition,
                () -> this.condition
        ));
        this.condition.collectConnectors(conditionOffset, aY, font, collector);

        int headHeight = this.getHeadHeight();
        collector.accept(new CommonBranchBlockConnector(
                aX + 6,
                aY + headHeight,
                this::setBody,
                () -> this.conditionBody,
                font,
                collector
        ));
        int yOffset = headHeight + this.getBodyHeight();
        int cOffset = RenderHelper.getPartialWidth(font, "§else_if", Map.of(), "condition");
        for (ElseIfEntry elseIf : this.elseIfs) {
            collector.accept(new SingletonExprConnector(
                    aX + 4 + cOffset,
                    aY + yOffset,
                    elseIf::setCondition,
                    elseIf::condition
            ));
            elseIf.condition.collectConnectors(aX + 4, aY + yOffset, font, collector);
            yOffset += getElseIfHeadHeight(elseIf);
            collector.accept(new CommonBranchBlockConnector(
                    aX + 6,
                    aY + yOffset,
                    elseIf::setBody,
                    elseIf::body,
                    font,
                    collector
            ));
            yOffset += getElseifBodyHeight(elseIf);
        }
        if (this.elseVisible) {
            collector.accept(new CommonBranchBlockConnector(
                    aX + 6,
                    aY + yOffset + this.getElseHeadHeight(),
                    this::setElseBody,
                    () -> this.elseBody,
                    font,
                    collector
            ));
        }
        super.collectConnectors(aX, aY, font, collector);
    }

    //region size
    //TODO store sizes, this is getting ridiculous
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
        for (ElseIfEntry elseIf : this.elseIfs) {
            int i = this.getElseIfHeadWidth(font, elseIf);
            if (i > width)
                width = i;
            if (elseIf.body != null) {
                int j = elseIf.body.getWidth(font) + 6;
                if (j > width) {
                    width = j;
                }
            }
        }
        return width;
    }

    private int getGlobalHeadWidth(Font font) {
        int width = this.getHeadWidth(font);
        if (elseVisible) {
            int i = this.getElseHeadWidth(font);
            if (i > width)
                width = i;
        }
        return Math.max(width, this.elseIfs.stream().mapToInt(e -> this.getElseIfHeadWidth(font, e)).max().orElse(0));
    }

    private int getHeadWidth(Font font) {
        return 4 + RenderHelper.getVisualTextWidth(font, "§if", Map.of("condition", condition));
    }

    private int getElseHeadWidth(Font font) {
        return 4 + RenderHelper.getVisualTextWidth(font, "§else", Map.of());
    }

    private int getElseIfHeadWidth(Font font, ElseIfEntry entry) {
        return 4 + RenderHelper.getVisualTextWidth(font, "§else_if", Map.of("condition", entry.condition));
    }

    @Override
    public int getHeight() {
        int height = getHeadHeight() + this.getBodyHeight();
        if (this.elseVisible) {
            height += this.getElseHeadHeight() + getElseBodyHeight();
        }
        for (ElseIfEntry elseIf : this.elseIfs) {
            height += this.getElseIfHeadHeight(elseIf) + getElseifBodyHeight(elseIf);
        }
        return height + 13; //height of the bottom enclose part - 3 for the offset
    }

    private int getBodyHeight() {
        return this.conditionBody == null ? 10 : this.conditionBody.getHeightWithChildren();
    }

    private int getElseBodyHeight() {
        return this.elseBody == null ? 10 : this.elseBody.getHeightWithChildren();
    }

    private int getElseifBodyHeight(ElseIfEntry entry) {
        return entry.body == null ? 10 : entry.body.getHeightWithChildren();
    }

    private int getHeadHeight() {
        return Math.max(18, this.condition.getHeight() + 4) + 2;
    }

    private int getElseHeadHeight() {
        return 18;
    }

    private int getElseIfHeadHeight(ElseIfEntry entry) {
        return Math.max(18, entry.condition.getHeight() + 4) + 2;
    }

    //endregion

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int globalHeadWidth = getGlobalHeadWidth(font);
        int headHeight = getHeadHeight();
        //head
        graphics.blitSprite(CodeWidgetSprites.LOOP_HEAD, renderX, renderY, globalHeadWidth, headHeight + 3);
        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 7 + (headHeight - 18) / 2, "§if", Map.of("condition", condition));

        //body
        int bodyHeight = getBodyHeight();
        if (this.conditionBody != null)
            this.conditionBody.render(graphics, font, renderX + 6, renderY + headHeight);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + headHeight + 3, 6, bodyHeight - 3);

        int endY = renderY + headHeight + bodyHeight;
        for (ElseIfEntry elseIf : this.elseIfs) {
            int elseIfHeadHeight = getElseIfHeadHeight(elseIf);
            graphics.blitSprite(CodeWidgetSprites.ELSE_CONDITION_HEAD, renderX, endY, globalHeadWidth, elseIfHeadHeight + 3);
            int elseIfBodyHeight = getElseifBodyHeight(elseIf);
            RenderHelper.renderVisualText(graphics, font, renderX + 4, endY + 7, "§else_if", Map.of("condition", elseIf.condition));
            if (elseIf.body != null) {
                elseIf.body.render(graphics, font, renderX + 6, endY + elseIfHeadHeight);
            }
            graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, endY + elseIfHeadHeight + 3, 6, elseIfBodyHeight - 3);
            endY += elseIfHeadHeight + elseIfBodyHeight;
        }

        if (elseVisible) {
            //else
            int elseHeadHeight = getElseHeadHeight();
            graphics.blitSprite(CodeWidgetSprites.ELSE_CONDITION_HEAD, renderX, endY, globalHeadWidth, elseHeadHeight + 3);
            int elseBodyHeight = getElseBodyHeight();
            RenderHelper.renderVisualText(graphics, font, renderX + 4, endY + 7, "§else", Map.of());
            if (this.elseBody != null) {
                this.elseBody.render(graphics, font, renderX + 6, endY + elseHeadHeight);
            }
            graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, endY + elseHeadHeight + 3, 6, elseBodyHeight - 3);
            endY += elseHeadHeight + elseBodyHeight;
        }
        //end
        renderScopeEnd(graphics, renderX, endY, globalHeadWidth);
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
        if (this.conditionBody != null) {
            this.conditionBody.registerInteractions(xOrigin + 6, yOrigin + getHeadHeight(), font, sink);
        }
        int h = getHeadHeight() + getBodyHeight();
        if (elseVisible) {
            if (this.elseBody != null) {
                this.elseBody.registerInteractions(xOrigin + 6, yOrigin + h + getElseHeadHeight(), font, sink);
            }
            h += getElseHeadHeight() + getElseBodyHeight();
        }
        sink.accept(new ModifyWidgetBranchesInteraction(xOrigin + getHeadWidth(font) - 9, yOrigin + h, 7, 7));
    }

    private class ModifyBranchesWidget extends PositionedWidget {

        protected ModifyBranchesWidget(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blitSprite(CodeWidgetSprites.METHOD_HEAD, x + 2, y + 2, 10, 10);
            int yOffset = 12;
            for (ElseIfEntry elseIf : IfWidget.this.elseIfs) {
                guiGraphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, x + 2, y + yOffset, 10, 10);
                yOffset += 10;
            }
            //TODO
            if (IfWidget.this.elseVisible) {
                guiGraphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, x + 2, y + yOffset, 10, 10);
            }
        }
    }

    private class ModifyWidgetBranchesInteraction extends CodeInteraction {

        protected ModifyWidgetBranchesInteraction(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void onClick(int mouseX, int mouseY, InteractionData callbacks) {
            callbacks.openWidget(new ModifyBranchesWidget(this.x + 10, this.y + 10, 50, 50));
        }
    }

    //region mod
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
    //endregion

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
        for (ElseIfEntry elseIf : this.elseIfs) {
            elseIf.condition.update(context);
            if (context != null)
                context.lvt.push();
            elseIf.body.update(context);
            if (context != null) {
                context.lvt.pop();
            }
        }
        super.update(context);
    }

    private static final class ElseIfEntry {
        private static final Codec<ElseIfEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                ExprCodeWidget.CODEC.optionalFieldOf("condition", ParamWidget.CONDITION).forGetter(ElseIfEntry::condition),
                BlockCodeWidget.CODEC.optionalFieldOf("body").forGetter(e -> Optional.ofNullable(e.body))
        ).apply(i, ElseIfEntry::fromCodec));

        private static ElseIfEntry fromCodec(ExprCodeWidget widget, Optional<BlockCodeWidget> blockCodeWidget) {
            return new ElseIfEntry(widget, blockCodeWidget.orElse(null));
        }

        private @NotNull ExprCodeWidget condition;
        private BlockCodeWidget body;

        private ElseIfEntry(@NotNull ExprCodeWidget condition, BlockCodeWidget body) {
            this.condition = condition;
            this.body = body;
        }

        public ExprCodeWidget condition() {
            return condition;
        }

        public void setCondition(ExprCodeWidget condition) {
            if (condition == null) {
                condition = ParamWidget.CONDITION;
            }
            this.condition = condition;
        }

        public BlockCodeWidget body() {
            return body;
        }

        public void setBody(BlockCodeWidget body) {
            this.body = body;
        }

        public ElseIfEntry copy() {
            return new ElseIfEntry(this.condition, this.body);
        }
    }

    public static class Builder implements BlockCodeWidget.Builder<IfWidget> {
        private ExprCodeWidget condition = ParamWidget.CONDITION;
        private final List<ElseIfEntry> elifs = new ArrayList<>();
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

        public Builder withElseIf(ExprCodeWidget condition) {
            this.elifs.add(new ElseIfEntry(condition, null));
            return this;
        }

        public Builder withElseIfNoCondition() {
            this.elifs.add(new ElseIfEntry(ParamWidget.CONDITION, null));
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
            return new IfWidget(child, condition, branch, showElse ? elseBranch : null, showElse, elifs);
        }
    }
}
