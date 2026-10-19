package net.kapitencraft.scripted.edit.graphical.widgets.stmt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.VarRefWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.fetch.ExprWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.fetch.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class HeadWidget extends StmtCodeWidget {
    public static final MapCodec<HeadWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            StmtCodeWidget.CODEC.optionalFieldOf("child").forGetter(w -> Optional.ofNullable(w.getChild())),
            Codec.STRING.fieldOf("name").forGetter(w -> w.name),
            Codec.unboundedMap(Codec.STRING, ExprCategory.CODEC).fieldOf("parameters").forGetter(w -> w.args.stream().collect(Collectors.toMap(VarRefWidget::getName, VarRefWidget::getExprCategory)))
    ).apply(i, HeadWidget::new));

    private final String name;
    private final List<VarRefWidget> args = new ArrayList<>();

    public HeadWidget(Optional<StmtCodeWidget> child, String name, Map<String, ExprCategory> map) {
        this.name = name;
        map.forEach((s, category) -> this.args.add(new VarRefWidget(s, category)));
        child.ifPresent(this::setChild);
    }

    public HeadWidget(StmtCodeWidget child, String name, List<VarRefWidget> params) {
        this.name = name;
        this.args.addAll(params);
        this.setChild(child);
    }

    public HeadWidget(StmtCodeWidget widget, String name) {
        this.name = name;
        this.setChild(widget);
    }

    @Override
    public StmtCodeWidget copy() {
        return new HeadWidget(
                getChildCopy(),
                this.name
        );
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
    }

    @Override
    public CodeWidget getByName(String arg) {
        return null;
    }

    @Override
    public void update(@Nullable MethodContext context, Font font) {
        MethodContext nC = new MethodContext();
        this.args.forEach(w -> w.update(nC, font));
        super.update(nC, font);
    }

    @Override
    protected @NotNull Type getType() {
        return Type.HEAD;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(CodeWidgetSprites.METHOD_HEAD, renderX, renderY, getWidth(font), 3 + getHeight());
        //var ref height = 18
        //ergo var offset = 10
        int globalYOffset = this.args.isEmpty() ? 3 : 7;
        int textY = renderY + 15 + globalYOffset;
        int paramY = renderY + 10 + globalYOffset;
        int xOffset = 4 + font.width(this.name);
        graphics.drawString(font, this.name, 4 + renderX, textY, 0, false);
        graphics.drawString(font, "(", renderX + xOffset, textY, 0, false);
        xOffset += font.width("(");
        List<VarRefWidget> varRefWidgets = this.args;
        for (int i = 0; i < varRefWidgets.size(); i++) {
            VarRefWidget value = varRefWidgets.get(i);
            value.render(graphics, font, renderX + xOffset, paramY);
            xOffset += value.getWidth(font);
            if (i != varRefWidgets.size() - 1) {
                graphics.drawString(font, ", ", renderX + xOffset, textY, 0, false);
                xOffset += font.width(", ");
            }
        }
        graphics.drawString(font, ")", renderX + xOffset, textY, 0, false);
        super.render(graphics, font, renderX, renderY);
    }

    @Override
    public int getWidth(Font font) {
        return Math.max(36, 6 +
                font.width(this.name) +
                font.width("(") + font.width(")") +
                font.width(", ") * Math.max(0, (this.args.size() - 1)) +
                this.args.stream().mapToInt(w -> w.getWidth(font)).sum());
    }

    @Override
    public int getHeight() {
        return this.args.isEmpty() ? 27 : 35;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (y < 8) return BlockWidgetFetchResult.notRemoved(this, x, y);
        if (y > this.getHeight()) return super.fetchAndRemoveHovered(x, y - this.getHeight(), font);
        if (x < this.getWidth(font)) {
            int oX = x;
            int paramOffset = 4 + font.width(this.name) + font.width("(");
            if (x < paramOffset)
                return BlockWidgetFetchResult.notRemoved(this, x, y);
            x -= paramOffset;
            int separatorWidth = font.width(", ");
            for (VarRefWidget arg : this.args) {
                if (x < arg.getWidth(font))
                    return new ExprWidgetFetchResult(true, x, y, arg.copy());
                x -= arg.getWidth(font);
                if (x < separatorWidth)
                    return BlockWidgetFetchResult.notRemoved(this, oX, y);
                x -= separatorWidth;
            }
        }
        return null;
    }

    public static class Builder implements StmtCodeWidget.Builder<HeadWidget> {
        private String translationKey;
        private final List<VarRefWidget> expr = new ArrayList<>();
        private StmtCodeWidget child;

        public Builder withParam(String paramName, ExprCategory widget) {
            expr.add(new VarRefWidget(paramName, widget));
            return this;
        }

        public Builder setName(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public Builder setChild(StmtCodeWidget widget) {
            this.child = widget;
            return this;
        }

        @Override
        public HeadWidget build() {
            return new HeadWidget(child, Objects.requireNonNull(translationKey, "missing translation key!"), expr);
        }
    }
}
