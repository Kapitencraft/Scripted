package net.kapitencraft.scripted.edit.graphical.widgets.stmt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.TextRenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.ArgumentStorage;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ParamWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.VarNameSelectorWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class AssignVarWidget extends StmtCodeWidget {
    public static final MapCodec<AssignVarWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i).and(
                    Codec.STRING.optionalFieldOf("name").forGetter(w -> Optional.ofNullable(w.varNameSelectorWidget.getSelected()))
            ).and(
                    ExprCodeWidget.CODEC.fieldOf("expr").forGetter(w -> w.expr)
            ).and(
                    Codec.BOOL.optionalFieldOf("create", false).forGetter(AssignVarWidget::createsVar)
            ).apply(i, AssignVarWidget::new)
    );

    private ExprCodeWidget expr;
    private final VarNameSelectorWidget varNameSelectorWidget = new VarNameSelectorWidget();

    private AssignVarWidget(StmtCodeWidget child, String varName, ExprCodeWidget expr, boolean createsVar) {
        this.expr = expr;
        this.setChild(child);
        this.varNameSelectorWidget.setSelected(varName);
        this.varNameSelectorWidget.setCreate(createsVar);
    }

    public AssignVarWidget(Optional<StmtCodeWidget> child, Optional<String> varName, ExprCodeWidget expr, boolean createVar) {
        this.expr = expr;
        child.ifPresent(this::setChild);
        varName.ifPresent(this.varNameSelectorWidget::setSelected);
        this.varNameSelectorWidget.setCreate(createVar);
    }

    @Override
    public StmtCodeWidget copy() {
        return new AssignVarWidget(
                getChildCopy(),
                this.varNameSelectorWidget.getVisualSelected(),
                this.expr.copy(),
                this.varNameSelectorWidget.doesCreateVar()
        );
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        if (!"value".equals(arg)) {
            throw new IllegalArgumentException("unknown argument in VarModWidget: " + arg);
        }
        this.expr = obj;
    }

    @Override
    public CodeWidget getByName(String arg) {
        if ("value".equals(arg)) {
            return this.expr;
        }
        throw new IllegalArgumentException("unknown argument in VarModWidget: " + arg);
    }

    @Override
    protected @NotNull Type getType() {
        return Type.BODY;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int height = getHeight();
        graphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, renderX, renderY, getWidth(font), 3 + height);
        TextRenderHelper.renderVisualText(graphics, font, renderX, renderY + 7 + (getHeight() - 20) / 2, getTranslationKey(), Map.of("var", varNameSelectorWidget, "value", expr));
        super.render(graphics, font, renderX, renderY);
    }

    @Override
    public int getWidth(Font font) {
        return 6 + TextRenderHelper.getVisualTextWidth(font, getTranslationKey(), Map.of("var", varNameSelectorWidget, "value", expr));
    }

    @Override
    public int getHeight() {
        return Math.max(18, this.expr.getHeight() + 4) + 2;
    }

    public static Builder builder() {
        return new Builder();
    }

    private boolean createsVar() {
        return this.varNameSelectorWidget.doesCreateVar();
    }

    private String getTranslationKey() {
        return createsVar() ? "§initialize" : "§assign";
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y > this.getHeight()) return super.fetchAndRemoveHovered(x, y - this.getHeight(), font);
        if (x < this.getWidth(font))
            return BlockWidgetFetchResult.fromExprList(4, x, y, font, this, getTranslationKey(),
                    ArgumentStorage.createDouble(
                            "var", null, () -> this.varNameSelectorWidget,
                            "value", this::setExpr, () -> this.expr
                    )
            );
        return null;
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        this.expr.registerInteractions(xOrigin, yOrigin, font, sink);
        this.varNameSelectorWidget.registerInteractions(
                xOrigin + 4 + TextRenderHelper.getPartialWidth(font, getTranslationKey(), Map.of(), "var"),
                yOrigin + 4 + (getHeight() - 20) / 2,
                font,
                sink
        );
        //sink.accept();
        super.registerInteractions(xOrigin, yOrigin, font, sink);
    }

    public void setExpr(ExprCodeWidget widget) {
        if (widget == null) widget = ParamWidget.OBJ; //TODO add dynamic type support
        this.expr = widget;
    }

    @Override
    public void update(@Nullable MethodContext context, Font font) {
        this.varNameSelectorWidget.update(context, font);
        if (this.expr instanceof ParamWidget) {
            this.expr = new ParamWidget(this.varNameSelectorWidget.getCategory());
        }
        this.expr.update(context, font);
        super.update(context, font);
    }

    public static class Builder implements StmtCodeWidget.Builder<AssignVarWidget> {
        private StmtCodeWidget child;
        private String varName;
        private ExprCodeWidget expr = ParamWidget.NUM;
        private boolean createVar = false;

        @Override
        public AssignVarWidget build() {
            return new AssignVarWidget(child, varName, expr, createVar);
        }

        public Builder setExpr(ExprCodeWidget value) {
            this.expr = value;
            return this;
        }

        public Builder setExpr(ExprCodeWidget.Builder<?> builder) {
            return this.setExpr(builder.build());
        }

        public Builder setChild(StmtCodeWidget.Builder<?> builder) {
            this.child = builder.build();
            return this;
        }

        public Builder doesCreateVar() {
            this.createVar = true;
            return this;
        }

        public Builder setName(String name) {
            this.varName = name;
            return this;
        }
    }
}
