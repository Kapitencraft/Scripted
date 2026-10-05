package net.kapitencraft.scripted.edit.graphical.widgets.stmt.loop;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.CodeWidgetHelper;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.connector.SingletonExprConnector;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.ArgumentStorage;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ParamWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.kapitencraft.scripted.edit.graphical.widgets.stmt.StmtCodeWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class WhileStmtWidget extends LoopStmtWidget {
    public static final MapCodec<WhileStmtWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            StmtCodeWidget.commonFields(i).and(
                    ExprCodeWidget.CODEC.fieldOf("condition").orElseGet(ParamWidget.CONDITION).forGetter(w -> w.condition)
            ).and(
                    StmtCodeWidget.CODEC.optionalFieldOf("body").forGetter(w -> Optional.ofNullable(w.body))
            ).apply(i, WhileStmtWidget::new)
    );

    @NotNull
    private ExprCodeWidget condition;
    private @Nullable StmtCodeWidget body;

    public WhileStmtWidget(@NotNull ExprCodeWidget condition, @Nullable StmtCodeWidget body) {
        this.condition = condition;
        this.body = body;
    }

    private WhileStmtWidget(StmtCodeWidget child, @NotNull ExprCodeWidget condition, @Nullable StmtCodeWidget body) {
        Preconditions.checkNotNull(condition);
        this.condition = condition;
        this.body = body;
        this.setChild(child);
    }

    public WhileStmtWidget(Optional<StmtCodeWidget> blockWidget, @NotNull ExprCodeWidget condition, Optional<StmtCodeWidget> body) {
        blockWidget.ifPresent(this::setChild);
        Preconditions.checkNotNull(condition);
        this.condition = condition;
        this.body = body.orElse(null);
    }

    @Override
    public StmtCodeWidget copy() {
        return new WhileStmtWidget(
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
    public CodeWidget getByName(String arg) {
        if ("condition".equals(arg)) {
            return this.condition;
        }
        throw new IllegalArgumentException("unknown argument " + arg + " in While");
    }

    @Override
    protected @NotNull Type getType() {
        return Type.WHILE_STMT;
    }

    @Override
    public void collectConnectors(Font font, Consumer<Connector> collector) {
        collector.accept(new SingletonExprConnector(
                x + 6 + CodeWidgetHelper.getPartialWidth(font, "§while", Map.of(), "condition"),
                y,
                this::setCondition,
                () -> this.condition
        ));
        super.collectConnectors(font, collector);
    }

    @Override
    void renderHead(GuiGraphics graphics, Font font, int renderX, int renderY) {
        CodeWidgetHelper.renderVisualText(graphics, font, renderX, renderY, "§while", Map.of("condition", this.condition));
    }

    protected int getHeadHeight() {
        return Math.max(18, this.condition.getHeight() + 4) + 2;
    }

    protected int getHeadWidth(Font font) {
        return 4 + CodeWidgetHelper.getVisualTextWidth(font, "§while", Map.of("condition", this.condition));
    }

    public void setBody(@Nullable StmtCodeWidget target) {
        this.body = target;
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < this.getHeadHeight()) {
            return BlockWidgetFetchResult.fromExprList(4, x, y, font, this, "§while",
                    ArgumentStorage.createSingle("condition", this::setCondition, () -> this.condition)
            );
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
    public void registerInteractions(Font font, Consumer<CodeInteraction> sink) {
        if (this.body != null) {
            this.body.registerInteractions(font, sink);
        }
        super.registerInteractions(font, sink);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setCondition(@Nullable ExprCodeWidget target) {
        this.condition = target == null ? ParamWidget.CONDITION.get() : target;
    }

    public static class Builder implements StmtCodeWidget.Builder<WhileStmtWidget> {
        private StmtCodeWidget child;
        private ExprCodeWidget condition = ParamWidget.CONDITION.get();
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
        public WhileStmtWidget build() {
            return new WhileStmtWidget(child, condition, body);
        }
    }

    @Override
    public void update(@Nullable MethodContext context, Font font, int x, int y) {
        CodeWidgetHelper.updateVisualText(context, font, x, y, "§while", Map.of("condition", this.condition));
        super.update(context, font, x, y);
    }
}