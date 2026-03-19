package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.code.CodeParser;
import net.kapitencraft.scripted.edit.graphical.code.StmtCodeVisitor;
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
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class WhileLoopWidget extends StmtCodeWidget {
    public static final MapCodec<WhileLoopWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            StmtCodeWidget.commonFields(i).and(
                    ExprCodeWidget.CODEC.optionalFieldOf("condition", ParamWidget.CONDITION).forGetter(w -> w.condition)
            ).and(
                    StmtCodeWidget.CODEC.optionalFieldOf("body").forGetter(w -> Optional.ofNullable(w.body))
            ).apply(i, WhileLoopWidget::new)
    );

    @NotNull
    private ExprCodeWidget condition;
    private @Nullable StmtCodeWidget body;

    public WhileLoopWidget(@NotNull ExprCodeWidget condition, @Nullable StmtCodeWidget body) {
        this.condition = condition;
        this.body = body;
    }

    private WhileLoopWidget(StmtCodeWidget child, @NotNull ExprCodeWidget condition, @Nullable StmtCodeWidget body) {
        Preconditions.checkNotNull(condition);
        this.condition = condition;
        this.body = body;
        this.setChild(child);
    }

    public WhileLoopWidget(Optional<StmtCodeWidget> blockWidget, @NotNull ExprCodeWidget condition, Optional<StmtCodeWidget> body) {
        blockWidget.ifPresent(this::setChild);
        Preconditions.checkNotNull(condition);
        this.condition = condition;
        this.body = body.orElse(null);
    }

    @Override
    public StmtCodeWidget copy() {
        return new WhileLoopWidget(
                this.getChildCopy(),
                this.condition.copy(),
                this.body != null ? this.body.copy() : null
        );
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        if ("condition".equals(arg)) {
            this.condition = obj;
        }
    }

    @Override
    public CodeWidget getByName(String argName) {
        if ("condition".equals(argName)) {
            return this.condition;
        }
        throw new IllegalArgumentException("unknown argument " + argName + " in While");
    }

    @Override
    protected @NotNull Type getType() {
        return Type.WHILE_LOOP;
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        collector.accept(new SingletonExprConnector(
                aX + 6 + RenderHelper.getPartialWidth(font, "§while", Map.of(), "condition"),
                aY,
                this::setCondition,
                () -> this.condition
        ));
        collector.accept(new CommonBranchBlockConnector(
                aX + 6,
                aY + this.getHeadHeight(),
                this::setBody,
                () -> this.body,
                font,
                collector
        ));
        super.collectConnectors(aX, aY, font, collector);
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int loopWidth = getHeadWidth(font);
        int headHeight = getHeadHeight();
        graphics.blitSprite(CodeWidgetSprites.LOOP_HEAD, renderX, renderY, loopWidth, headHeight + 3);
        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 7 + (headHeight - 18) / 2, "§while", Map.of("condition", this.condition));
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
        return 4 + RenderHelper.getVisualTextWidth(font, "§while", Map.of("condition", this.condition));
    }

    @Override
    public int getWidth(Font font) {
        int width = getHeadWidth(font);
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

    public void setBody(@Nullable StmtCodeWidget target) {
        this.body = target;
    }

    public void insertBodyMiddle(StmtCodeWidget widget) {
        widget.setChild(this.body);
        this.body = widget;
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < this.getHeadHeight()) {
            return BlockWidgetFetchResult.fromExprList(4, x, y, font, this, "§while", ArgumentStorage.createSingle("condition", this::setCondition, () -> this.condition));
        } else if (y > this.getHeight()) {
            return super.fetchAndRemoveHovered(x, y - this.getHeight(), font);
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

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {

    }

    public static Builder builder() {
        return new Builder();
    }

    public void setCondition(@Nullable ExprCodeWidget target) {
        this.condition = target == null ? ParamWidget.CONDITION : target;
    }

    public static class Builder implements StmtCodeWidget.Builder<WhileLoopWidget> {
        private StmtCodeWidget child;
        private ExprCodeWidget condition = ParamWidget.CONDITION;
        private StmtCodeWidget body;

        public Builder setBody(StmtCodeWidget.Builder<?> widget) {
            this.body = widget.build();
            return this;
        }

        public Builder setChild(StmtCodeWidget.Builder<?> widget) {
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

    @Override
    public void update(@Nullable MethodContext context) {
        this.condition.update(context);
        if (this.body != null) {
            if (context != null) {
                context.lvt.push();
            }
            this.body.update(context);
            if (context != null) {
                context.lvt.pop();
            }
        }
        super.update(context);
    }

    private static final class CodeVisitor implements StmtCodeVisitor<WhileLoopWidget, Stmt.While> {

        @Override
        public Stmt.While parse(WhileLoopWidget widget) {
            return new Stmt.While(
                    CodeParser.parseExpr(widget.condition),
                    CodeParser.parseOptionalStmtList(widget.body),
                    Token.createNative("whi")
            );
        }
    }
}