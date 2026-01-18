package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.MapCodec;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParamWidget implements ExprCodeWidget {
    public static final MapCodec<ParamWidget> CODEC = ExprCategory.CODEC.xmap(ParamWidget::new, w -> w.exprCategory).fieldOf("category");

    public static final ParamWidget CONDITION = new ParamWidget(ExprCategory.BOOLEAN);
    public static final ExprCodeWidget OBJ = new ParamWidget(ExprCategory.OTHER);
    public static final ExprCodeWidget NUM = new ParamWidget(ExprCategory.NUMBER);

    private final ExprCategory exprCategory;

    public ParamWidget(ExprCategory exprCategory) {
        this.exprCategory = exprCategory;
    }

    @Override
    public ExprCodeWidget copy() {
        return this; //param widgets should be treated as singletons
    }

    @Override
    public GhostInserter getGhostWidgetTarget(int x, int y, Font font, boolean isBlock) {
        return null;
    }

    @Override
    public @NotNull Type getType() {
        return Type.PARAM;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(exprCategory.getSpriteLocation(), renderX, renderY, 14, 12);
    }

    @Override
    public int getWidth(Font font) {
        return 14;
    }

    @Override
    public int getHeight() {
        return 12;
    }


    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }
}
