package net.kapitencraft.scripted.edit.graphical.widgets;

import com.mojang.serialization.MapCodec;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParamWidget implements CodeWidget {
    public static final MapCodec<ParamWidget> CODEC = ExprCategory.CODEC.xmap(ParamWidget::new, w -> w.exprCategory).fieldOf("category");

    public static final ParamWidget CONDITION = new ParamWidget(ExprCategory.BOOLEAN);

    private final ExprCategory exprCategory;

    public ParamWidget(ExprCategory exprCategory) {
        this.exprCategory = exprCategory;
    }

    @Override
    public CodeWidget copy() {
        return this; //param widgets should be treated as singletons
    }

    @Override
    public @NotNull Type getType() {
        return Type.PARAM;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(exprCategory.getSpriteLocation(), renderX, renderY - 2, 14, 12);
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
