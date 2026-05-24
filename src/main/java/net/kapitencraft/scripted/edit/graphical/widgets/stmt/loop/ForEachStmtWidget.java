package net.kapitencraft.scripted.edit.graphical.widgets.stmt.loop;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.graphical.TextRenderHelper;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ForEachStmtWidget extends LoopStmtWidget {
    public static final MapCodec<ForEachStmtWidget> CODEC = RecordCodecBuilder.mapCodec(i -> loopCommonFields(i)
            .and(ExprCodeWidget.CODEC.optionalFieldOf("expr", ParamWidget.OBJ).forGetter(w -> w.values))
            .apply(i, ForEachStmtWidget::new)
    );

    private ExprCodeWidget values = ParamWidget.OBJ;
    private final Map<String, ExprCodeWidget> args;
    private final VarNameSelectorWidget varName = new VarNameSelectorWidget();

    public ForEachStmtWidget(Optional<StmtCodeWidget> child, Optional<StmtCodeWidget> body, ExprCodeWidget values) {
        this(values);
        child.ifPresent(this::setChild);
        body.ifPresent(this::setBody);
    }

    public ForEachStmtWidget(ExprCodeWidget values) {
        Preconditions.checkNotNull(values);
        this.values = values;
        this.args = new HashMap<>();
        this.args.put("var", varName);
        this.args.put("expr", values);
    }

    public ForEachStmtWidget(ExprCodeWidget values, StmtCodeWidget body) {
        this(values);
        this.body = body;
    }

    protected int getHeadHeight() {
        return Math.max(18, ExprCodeWidget.getHeightFromArgs(this.args) + 4) + 2;
    }

    protected int getHeadWidth(Font font) {
        return 6 + TextRenderHelper.getVisualTextWidth(font, "§for_each", this.args);
    }

    @Override
    protected @NotNull Type getType() {
        return Type.FOR_EACH_STMT;
    }

    @Override
    public StmtCodeWidget copy() {
        return new ForEachStmtWidget(
                this.values.copy()
        );
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        if ("expr".equals(arg)) {
            this.values = obj;
        }
    }

    @Override
    public CodeWidget getByName(String arg) {
        if ("expr".equals(arg))
            return values;
        throw new IllegalStateException("unknown argument \"" + arg + "\" on ForEach widget");
    }


    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        ArgumentExprConnector.parse(font, aX + 4, aY, "§for_each", this.args, this, collector);
        super.collectConnectors(aX, aY, font, collector);
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < this.getHeadHeight()) {
            if (x < this.getHeadWidth(font))
                return BlockWidgetFetchResult.fromExprList(4, x, y, font, this, "§for_each", ArgumentStorage.create(this.args));
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
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        TextRenderHelper.registerAllInteractions(xOrigin + 4, yOrigin + 7 + (getHeadHeight() - 20) / 2, font, sink, "§for_each", args);
        super.registerInteractions(xOrigin, yOrigin, font, sink);
    }

    @Override
    public void renderHead(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int headHeight = getHeadHeight();

        TextRenderHelper.renderVisualText(graphics, font,
                renderX,
                renderY + 7 + (headHeight - 20) / 2,
                "§for_each", args
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements StmtCodeWidget.Builder<ForEachStmtWidget> {
        private ExprCodeWidget values = ParamWidget.OBJ;
        private StmtCodeWidget body;

        public Builder setValues(ExprCodeWidget widget) {
            this.values = widget;
            return this;
        }

        @Override
        public ForEachStmtWidget build() {
            return new ForEachStmtWidget(values, body);
        }
    }
}
