package net.kapitencraft.scripted.edit.graphical.widgets.stmt.loop;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.TextRenderHelper;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.ArgumentExprConnector;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.ArgumentStorage;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ParamWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.VarNameSelectorWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.kapitencraft.scripted.edit.graphical.widgets.stmt.StmtCodeWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ForRangeStmtWidget extends LoopStmtWidget {
    public static final MapCodec<ForRangeStmtWidget> CODEC = RecordCodecBuilder.mapCodec(i -> commonFields(i)
            .and(ExprCodeWidget.CODEC.optionalFieldOf("min", ParamWidget.NUM).forGetter(ForRangeStmtWidget::getMin))
            .and(ExprCodeWidget.CODEC.optionalFieldOf("max", ParamWidget.NUM).forGetter(ForRangeStmtWidget::getMax))
            .and(ExprCodeWidget.CODEC.optionalFieldOf("step", ParamWidget.NUM).forGetter(ForRangeStmtWidget::getStep))
            .and(StmtCodeWidget.CODEC.optionalFieldOf("body").forGetter(w -> Optional.ofNullable(w.body)))
            .apply(i, ForRangeStmtWidget::new)
    );

    private final VarNameSelectorWidget varName = new VarNameSelectorWidget();
    private final Map<String, ExprCodeWidget> args;
    private StmtCodeWidget body;

    private ForRangeStmtWidget(Optional<StmtCodeWidget> child, ExprCodeWidget min, ExprCodeWidget max, ExprCodeWidget step, Optional<StmtCodeWidget> body) {
        this(min, max, step, body.orElse(null));
        child.ifPresent(this::setChild);
    }

    private ForRangeStmtWidget(ExprCodeWidget lowerBound, ExprCodeWidget upperBound, ExprCodeWidget step, StmtCodeWidget body) {
        this.args = new HashMap<>();
        args.put("var", varName);
        args.put("min", lowerBound);
        args.put("max", upperBound);
        args.put("interval", step);
        this.varName.setCreate(true);
        this.body = body;
    }

    @Override
    protected @NotNull Type getType() {
        return Type.FOR_RANGE_STMT;
    }

    protected int getHeadHeight() {
        return Math.max(18, ExprCodeWidget.getHeightFromArgs(this.args) + 4) + 2;
    }

    protected int getHeadWidth(Font font) {
        return 6 + TextRenderHelper.getVisualTextWidth(font, "§for_range", this.args);
    }

    @Override
    public StmtCodeWidget copy() {
        return new ForRangeStmtWidget(
                this.args.get("min").copy(),
                this.args.get("max").copy(),
                this.args.get("interval").copy(),
                this.body == null ? null : this.body.copy()
        );
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        if ("var".equals(arg) || !args.containsKey(arg))
            throw new IllegalStateException("unknown argument \"" + arg + "\" on ForRange widget");
        this.args.put(arg, obj);
    }

    @Override
    public CodeWidget getByName(String arg) {
        if ("var".equals(arg) || !args.containsKey(arg))
            throw new IllegalStateException("unknown argument \"" + arg + "\" on ForRange widget");
        return args.get(arg);
    }

    public ExprCodeWidget getMin() {
        return args.get("min");
    }

    public ExprCodeWidget getMax() {
        return args.get("max");
    }

    public ExprCodeWidget getStep() {
        return args.get("interval");
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        ArgumentExprConnector.parse(font, aX + 4, aY, "§for_range", this.args, this, collector);
        super.collectConnectors(aX, aY, font, collector);
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < this.getHeadHeight()) {
            if (x < this.getHeadWidth(font))
                return BlockWidgetFetchResult.fromExprList(4, x, y, font, this, "§for", ArgumentStorage.create(this.args));
            return null;
        }

        if (y - this.getHeadHeight() < getBodyHeight()) {
            if (x < 6)
                return BlockWidgetFetchResult.notRemoved(this, x, y);
            if (this.body != null) {
                WidgetFetchResult result = this.body.fetchAndRemoveHovered(x - 6, y - this.getHeadHeight(), font);
                if (result == null) return null;
                if (!result.removed())
                    this.body = null;
                return result.setRemoved();
            }
            return null;
        }

        return super.fetchAndRemoveHovered(x, y - getHeight(), font);
    }

    @Override
    public void renderHead(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int headHeight = getHeadHeight();

        TextRenderHelper.renderVisualText(graphics, font,
                renderX,
                renderY + 7 + (headHeight - 20) / 2,
                "§for_range", args
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        TextRenderHelper.registerAllInteractions(xOrigin + 4, yOrigin + 7 + (getHeadHeight() - 20) / 2, font, sink, "§for_range", args);

        super.registerInteractions(xOrigin, yOrigin, font, sink);
    }

    @Override
    public void update(@Nullable MethodContext context, Font font) {
        this.args.values().forEach(w -> w.update(context, font));
        super.update(context, font);
    }

    public static class Builder implements StmtCodeWidget.Builder<ForRangeStmtWidget> {
        private ExprCodeWidget min = ParamWidget.NUM;
        private ExprCodeWidget max = ParamWidget.NUM;
        private ExprCodeWidget step = ParamWidget.NUM;
        private StmtCodeWidget body;

        public Builder setMin(ExprCodeWidget widget) {
            this.min = widget;
            return this;
        }

        public Builder setMax(ExprCodeWidget max) {
            this.max = max;
            return this;
        }

        public Builder setStep(ExprCodeWidget step) {
            this.step = step;
            return this;
        }

        @Override
        public ForRangeStmtWidget build() {
            return new ForRangeStmtWidget(min, max, step, body);
        }
    }
}
