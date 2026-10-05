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
import java.util.function.Supplier;

public class ParamWidget implements ExprCodeWidget {
    public static final MapCodec<ParamWidget> CODEC = ExprCategory.CODEC.xmap(ParamWidget::new, w -> w.exprCategory).fieldOf("category");

    public static final Supplier<ExprCodeWidget> CONDITION = () -> new ParamWidget(ExprCategory.BOOLEAN);
    public static final Supplier<ExprCodeWidget> OBJ = () -> new ParamWidget(ExprCategory.OTHER);
    public static final Supplier<ExprCodeWidget> NUM = () -> new ParamWidget(ExprCategory.NUMBER);

    private final ExprCategory exprCategory;
    private int x, y;

    public ParamWidget(ExprCategory exprCategory) {
        this.exprCategory = exprCategory;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
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
    public CodeWidget getByName(String arg) {
        throw new IllegalAccessError("can not get from param widget");
    }

    @Override
    public void collectConnectors(Font font, Consumer<Connector> collector) {

    }

    @Override
    public void update(@Nullable MethodContext context, Font font, int x, int y) {

    }

    @Override
    public @NotNull Type getType() {
        return Type.PARAM;
    }

    @Override
    public void render(GuiGraphics graphics, Font font) {
        graphics.blitSprite(exprCategory.getSpriteLocation(), x, y, 14, 12);
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
    public void registerInteractions(Font font, Consumer<CodeInteraction> sink) {}
}
