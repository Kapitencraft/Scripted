package net.kapitencraft.scripted.edit.graphical.widgets.stmt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.kap_lib.core.client.widget.PositionedWidget;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.TextRenderHelper;
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

public class IfStmtWidget extends StmtCodeWidget {
    public static final MapCodec<IfStmtWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i).and(
                    ExprCodeWidget.CODEC.optionalFieldOf("condition", ParamWidget.CONDITION).forGetter(w -> w.condition)
            ).and(
                    StmtCodeWidget.CODEC.optionalFieldOf("condition_body").forGetter(w -> Optional.ofNullable(w.conditionBody))
            ).and(
                    Codec.BOOL.optionalFieldOf("show_else", true).forGetter(w -> w.elseVisible)
            ).and(
                    StmtCodeWidget.CODEC.optionalFieldOf("else_body").forGetter(w -> Optional.ofNullable(w.elseBody))
            ).and(
                    ElseIfEntry.CODEC.listOf().fieldOf("elifs").forGetter(w -> w.elseIfs)
            ).apply(i, IfStmtWidget::new)
    );

    private ExprCodeWidget condition;
    private @Nullable StmtCodeWidget conditionBody;
    private boolean elseVisible;
    private @Nullable StmtCodeWidget elseBody;
    private final List<ElseIfEntry> elseIfs = new ArrayList<>();

    //region size attributes
    private int globalWidth;
    private int globalHeadWidth;
    private int headWidth;
    private int elseHeadWidth;
    private int headHeight;
    //endregion

    public IfStmtWidget(ExprCodeWidget condition) {
        this.condition = condition;
    }

    private IfStmtWidget(StmtCodeWidget child, ExprCodeWidget condition, @Nullable StmtCodeWidget conditionBody, @Nullable StmtCodeWidget elseBody, boolean showElse, List<ElseIfEntry> elifs) {
        this(condition);
        this.conditionBody = conditionBody;
        this.elseBody = elseBody;
        this.setChild(child);
        this.elseVisible = showElse;
        this.elseIfs.addAll(elifs);
    }

    public IfStmtWidget(Optional<StmtCodeWidget> child, ExprCodeWidget headWidgets, Optional<StmtCodeWidget> conditionBody, boolean elseVisible, Optional<StmtCodeWidget> elseBody, List<ElseIfEntry> elseIfs) {
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
    public StmtCodeWidget copy() {
        IfStmtWidget widget = new IfStmtWidget(
                this.getChildCopy(),
                this.condition.copy(),
                this.conditionBody != null ? this.conditionBody.copy() : null,
                this.elseBody != null ? this.elseBody.copy() : null,
                this.elseVisible,
                copyElifs(this.elseIfs)
        );
        widget.globalWidth = this.globalWidth;
        widget.globalHeadWidth = this.globalHeadWidth;
        widget.headWidth = this.headWidth;
        widget.elseHeadWidth = this.elseHeadWidth;
        widget.headHeight = this.headHeight;
        return widget;
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
    public CodeWidget getByName(String arg) {
        if ("condition".equals(arg)) {
            return this.condition;
        }
        if (arg.startsWith("elif-condition")) {
            int idx = Integer.parseInt(arg.substring(14));
            return this.elseIfs.get(idx).condition;
        }
        throw new IllegalArgumentException("unknown argument named " + arg + " in If");
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected @NotNull Type getType() {
        return Type.IF_STMT;
    }

    //region size
    @Override
    public int getWidth(Font font) {
        return this.globalWidth;
    }

    private int calculateWidth(Font font) {
        int width = this.headWidth;
        int elseHeadWidth = this.elseHeadWidth;
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
            int i = elseIf.headWidth;
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

    private int getGlobalHeadWidth() {
        int width = headWidth;
        if (elseVisible) {
            int i = this.elseHeadWidth;
            if (i > width)
                width = i;
        }
        return Math.max(width, this.elseIfs.stream().mapToInt(e -> e.headWidth).max().orElse(0));
    }

    private int getHeadWidth(Font font) {
        return 4 + TextRenderHelper.getVisualTextWidth(font, "§if", Map.of("condition", condition));
    }

    private int getElseHeadWidth(Font font) {
        return 4 + TextRenderHelper.getVisualTextWidth(font, "§else", Map.of());
    }

    private int getElseIfHeadWidth(Font font, ElseIfEntry entry) {
        return 4 + TextRenderHelper.getVisualTextWidth(font, "§else_if", Map.of("condition", entry.condition));
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
        return headHeight;
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
        int globalHeadWidth = this.globalHeadWidth;
        int headHeight = getHeadHeight();
        //head
        graphics.blitSprite(CodeWidgetSprites.SCOPE_HEAD, renderX, renderY, globalHeadWidth, headHeight + 3);
        TextRenderHelper.renderVisualText(graphics, font, renderX, renderY + 7 + (headHeight - 20) / 2, "§if", Map.of("condition", condition));

        //body
        int bodyHeight = getBodyHeight();
        if (this.conditionBody != null)
            this.conditionBody.render(graphics, font, renderX + 6, renderY + headHeight);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, renderY + headHeight + 3, 6, bodyHeight - 3);

        int endY = renderY + headHeight + bodyHeight;
        for (ElseIfEntry elseIf : this.elseIfs) {
            int elseIfHeadHeight = getElseIfHeadHeight(elseIf);
            graphics.blitSprite(CodeWidgetSprites.SCOPE_BOTH_SIDE, renderX, endY, globalHeadWidth, elseIfHeadHeight + 3);
            int elseIfBodyHeight = getElseifBodyHeight(elseIf);
            TextRenderHelper.renderVisualText(graphics, font, renderX, endY + 7, "§else_if", Map.of("condition", elseIf.condition));
            if (elseIf.body != null) {
                elseIf.body.render(graphics, font, renderX + 6, endY + elseIfHeadHeight);
            }
            graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, renderX, endY + elseIfHeadHeight + 3, 6, elseIfBodyHeight - 3);
            endY += elseIfHeadHeight + elseIfBodyHeight;
        }

        if (elseVisible) {
            //else
            int elseHeadHeight = getElseHeadHeight();
            graphics.blitSprite(CodeWidgetSprites.SCOPE_BOTH_SIDE, renderX, endY, globalHeadWidth, elseHeadHeight + 3);
            int elseBodyHeight = getElseBodyHeight();
            TextRenderHelper.renderVisualText(graphics, font, renderX, endY + 7, "§else", Map.of());
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

    //region interaction
    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        int conditionOffset = aX + 4 + TextRenderHelper.getPartialWidth(font, "§if", Map.of(), "condition");
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
        int cOffset = TextRenderHelper.getPartialWidth(font, "§else_if", Map.of(), "condition");
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

        int yOffset = this.getHeadHeight() + this.getBodyHeight();
        for (ElseIfEntry elseIf : this.elseIfs) {
            if (y - yOffset < this.getElseIfHeadHeight(elseIf)) {
                if (x < elseIf.headWidth)
                    return BlockWidgetFetchResult.fromExprList(4, x, y, font, this, "§else_if", ArgumentStorage.createSingle("condition", this::setCondition, () -> elseIf.condition));
            }
            if (y - yOffset - this.getElseIfHeadHeight(elseIf) < elseIf.getBodyHeight()) {
                if (x < 6)
                    return BlockWidgetFetchResult.notRemoved(this, x, y);
                if (elseIf.body != null) {
                    WidgetFetchResult result = elseIf.body.fetchAndRemoveHovered(x - 6,
                            y - this.getHeadHeight() - this.getBodyHeight() - this.getElseHeadHeight(), font);
                    if (result == null) return null;
                    if (!result.removed())
                        elseIf.body = null;
                    return result.setRemoved();
                }
                return null;
            }
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
        int xOffsetElseIfCondition = TextRenderHelper.getPartialWidth(font, "§else_if", Map.of(), "condition");
        for (ElseIfEntry elseIf : this.elseIfs) {
            elseIf.condition.registerInteractions(xOrigin + xOffsetElseIfCondition, yOrigin + h, font, sink);

            h += getElseIfHeadHeight(elseIf);
            if (elseIf.body != null) {
                elseIf.body.registerInteractions(xOrigin + 6, yOrigin + h, font, sink);
            }
            h += elseIf.getBodyHeight();
        }

        if (elseVisible) {
            if (this.elseBody != null) {
                this.elseBody.registerInteractions(xOrigin + 6, yOrigin + h + getElseHeadHeight(), font, sink);
            }
            h += getElseHeadHeight() + getElseBodyHeight();
        }
        sink.accept(new ModifyWidgetBranchesInteraction(xOrigin + getGlobalHeadWidth() - 9, yOrigin + h + 4, 7, 7));
        super.registerInteractions(xOrigin, yOrigin, font, sink);
    }

    private class ModifyBranchesWidget extends PositionedWidget {

        protected ModifyBranchesWidget(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blitSprite(CodeWidgetSprites.METHOD_HEAD, x + 2, y + 2, 10, 10);
            int yOffset = 12;
            for (ElseIfEntry elseIf : IfStmtWidget.this.elseIfs) {
                guiGraphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, x + 2, y + yOffset, 10, 10);
                yOffset += 10;
            }
            //TODO
            if (IfStmtWidget.this.elseVisible) {
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
    //endregion

    //region mod
    public void setBody(@Nullable StmtCodeWidget conditionBody) {
        this.conditionBody = conditionBody;
    }

    public void setElseBody(@Nullable StmtCodeWidget elseBody) {
        this.elseBody = elseBody;
    }

    public void insertBodyMiddle(StmtCodeWidget widget) {
        widget.setChild(this.conditionBody);
        this.conditionBody = widget;
    }

    public void insertElseMiddle(StmtCodeWidget widget) {
        widget.setChild(this.elseBody);
        this.elseBody = widget;
    }

    public void setCondition(@Nullable ExprCodeWidget target) {
        this.condition = target == null ? ParamWidget.CONDITION : target;
    }
    //endregion

    @Override
    public void update(@Nullable MethodContext context, Font font) {
        this.condition.update(context, font);
        if (this.conditionBody != null) {
            if (context != null)
                context.lvt.push();
            this.conditionBody.update(context, font);
            if (context != null) {
                context.lvt.pop();
            }
        }
        if (this.elseVisible) {
            if (this.elseBody != null) {
                if (context != null)
                    context.lvt.push();
                this.elseBody.update(context, font);
                if (context != null) {
                    context.lvt.pop();
                }
            }
        }
        for (ElseIfEntry elseIf : this.elseIfs) {
            elseIf.headWidth = getElseIfHeadWidth(font, elseIf);
            elseIf.condition.update(context, font);
            if (context != null)
                context.lvt.push();
            if (elseIf.body != null)
                elseIf.body.update(context, font);
            if (context != null) {
                context.lvt.pop();
            }
        }
        this.globalWidth = this.calculateWidth(font);
        this.headWidth = this.getHeadWidth(font);
        this.elseHeadWidth = this.getElseHeadWidth(font);
        this.globalHeadWidth = this.getGlobalHeadWidth();
        this.headHeight = Math.max(18, this.condition.getHeight() + 4) + 2;
        super.update(context, font);
    }

    private static final class ElseIfEntry {
        private static final Codec<ElseIfEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                ExprCodeWidget.CODEC.optionalFieldOf("condition", ParamWidget.CONDITION).forGetter(ElseIfEntry::condition),
                StmtCodeWidget.CODEC.optionalFieldOf("body").forGetter(e -> Optional.ofNullable(e.body))
        ).apply(i, ElseIfEntry::fromCodec));

        private static ElseIfEntry fromCodec(ExprCodeWidget widget, Optional<StmtCodeWidget> blockCodeWidget) {
            return new ElseIfEntry(widget, blockCodeWidget.orElse(null));
        }

        private @NotNull ExprCodeWidget condition;
        private StmtCodeWidget body;
        private int headWidth;
        private boolean ended;

        private ElseIfEntry(@NotNull ExprCodeWidget condition, StmtCodeWidget body) {
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

        public StmtCodeWidget body() {
            return body;
        }

        public void setBody(StmtCodeWidget body) {
            this.body = body;
        }

        public ElseIfEntry copy() {
            return new ElseIfEntry(this.condition, this.body);
        }

        private int getBodyHeight() {
            return body == null ? 10 : this.body.getHeight();
        }
    }

    public static class Builder implements StmtCodeWidget.Builder<IfStmtWidget> {
        private ExprCodeWidget condition = ParamWidget.CONDITION;
        private final List<ElseIfEntry> elifs = new ArrayList<>();
        private boolean showElse = true;
        private StmtCodeWidget child, branch, elseBranch;

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

        public Builder withChild(StmtCodeWidget.Builder<?> builder) {
            this.child = builder.build();
            return this;
        }

        public Builder withBranch(StmtCodeWidget.Builder<?> builder) {
            this.branch = builder.build();
            return this;
        }

        public Builder withElseBranch(StmtCodeWidget.Builder<?> builder) {
            this.elseBranch = builder.build();
            this.showElse = true;
            return this;
        }

        @Override
        public IfStmtWidget build() {
            return new IfStmtWidget(child, condition, branch, showElse ? elseBranch : null, showElse, elifs);
        }
    }
}
