package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.MapCodec;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        throw new IllegalAccessError("can not insert into param widget");
    }

    @Override
    public CodeWidget getByName(String argName) {
        throw new IllegalAccessError("can not get from param widget");
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {

    }

    @Override
    public void update(@Nullable MethodContext context) {

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

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {

    }
}
