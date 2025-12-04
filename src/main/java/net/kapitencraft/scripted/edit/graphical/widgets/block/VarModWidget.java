package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.ExprType;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.ParamWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Map;
import java.util.Optional;

public class VarModWidget extends BlockWidget {
    public static final MapCodec<VarModWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i).and(
                    Codec.STRING.fieldOf("name").forGetter(w -> w.varName)
            ).and(
                    CodeWidget.CODEC.fieldOf("expr").forGetter(w -> w.expr)
            ).apply(i, VarModWidget::new)
    );

    private final String varName;
    private final CodeWidget expr;

    private VarModWidget(BlockWidget child, String varName, CodeWidget expr) {
        this.expr = expr;
        this.setChild(child);
        this.varName = varName;
    }

    public VarModWidget(Optional<BlockWidget> child, String varName, CodeWidget expr) {
        this.expr = expr;
        child.ifPresent(this::setChild);
        this.varName = varName;
    }

    @Override
    public Type getType() {
        return Type.BODY;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int height = getHeight();
        graphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, renderX, renderY, 6 + getWidth(font), 3 + height);
        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 7 + Math.max(0, (height - 19)) >> 1, "§assign", Map.of("value", expr));
        super.render(graphics, font, renderX, renderY);
    }

    @Override
    public int getWidth(Font font) {
        return font.width(this.varName);
    }

    @Override
    public int getHeight() {
        return Math.max(19, 9 + 10);
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

    public static class Builder implements BlockWidget.Builder<VarModWidget> {
        private BlockWidget child;
        private String varName;
        private CodeWidget expr = new ParamWidget(ExprType.NUMBER);

        @Override
        public VarModWidget build() {
            return new VarModWidget(child, varName, expr);
        }

        public Builder setExpr(CodeWidget value) {
            this.expr = value;
            return this;
        }

        public Builder setExpr(CodeWidget.Builder<?> builder) {
            return this.setExpr(builder.build());
        }

        public Builder setChild(BlockWidget.Builder<?> builder) {
            this.child = builder.build();
            return this;
        }

        public Builder setName(String name) {
            this.varName = name;
            return this;
        }
    }
}
