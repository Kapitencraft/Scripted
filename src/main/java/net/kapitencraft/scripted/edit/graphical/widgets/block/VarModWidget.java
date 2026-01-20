package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ParamWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.VarNameSelectorWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class VarModWidget extends BlockCodeWidget {
    public static final MapCodec<VarModWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i).and(
                    Codec.STRING.optionalFieldOf("name").forGetter(w -> Optional.ofNullable(w.varNameSelectorWidget.getSelected()))
            ).and(
                    ExprCodeWidget.CODEC.fieldOf("expr").forGetter(w -> w.expr)
            ).apply(i, VarModWidget::new)
    );

    private ExprCodeWidget expr;
    private final VarNameSelectorWidget varNameSelectorWidget = new VarNameSelectorWidget();

    private VarModWidget(BlockCodeWidget child, String varName, ExprCodeWidget expr) {
        this.expr = expr;
        this.setChild(child);
        this.varNameSelectorWidget.setSelected(varName);
    }

    public VarModWidget(Optional<BlockCodeWidget> child, Optional<String> varName, ExprCodeWidget expr) {
        this.expr = expr;
        child.ifPresent(this::setChild);
        varName.ifPresent(this.varNameSelectorWidget::setSelected);
    }

    @Override
    public BlockCodeWidget copy() {
        return new VarModWidget(
                getChildCopy(),
                this.varNameSelectorWidget.getVisualSelected(),
                this.expr.copy()
        );
    }

    @Override
    protected @NotNull Type getType() {
        return Type.BODY;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int height = getHeight();
        graphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, renderX, renderY, 6 + getWidth(font), 3 + height);
        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 7 + Math.max(0, (height - 19)) >> 1, "§assign", Map.of("var", varNameSelectorWidget, "value", expr));
        super.render(graphics, font, renderX, renderY);
    }

    @Override
    public int getWidth(Font font) {
        return font.width(this.varNameSelectorWidget.getVisualSelected());
    }

    @Override
    public int getHeight() {
        return Math.max(18, this.expr.getHeight() + 4);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y > this.getHeight()) return fetchChildRemoveHovered(x, y - this.getHeight(), font);
        //if (x < this.getWidth(font)) return WidgetFetchResult.fromExprList(4, x, y, font, this, this.expr);
        return null;
    }

    public void setExpr(ExprCodeWidget widget) {
        if (widget == null) widget = ParamWidget.OBJ; //TODO add dynamic type support
        this.expr = widget;
    }

    @Override
    public void update(@Nullable MethodContext context) {
        this.varNameSelectorWidget.update(context);
        this.expr.update(context);
        super.update(context);
    }

    public static class Builder implements BlockCodeWidget.Builder<VarModWidget> {
        private BlockCodeWidget child;
        private String varName;
        private ExprCodeWidget expr = new ParamWidget(ExprCategory.NUMBER);

        @Override
        public VarModWidget build() {
            return new VarModWidget(child, varName, expr);
        }

        public Builder setExpr(ExprCodeWidget value) {
            this.expr = value;
            return this;
        }

        public Builder setExpr(ExprCodeWidget.Builder<?> builder) {
            return this.setExpr(builder.build());
        }

        public Builder setChild(BlockCodeWidget.Builder<?> builder) {
            this.child = builder.build();
            return this;
        }

        public Builder setName(String name) {
            this.varName = name;
            return this;
        }
    }
}
