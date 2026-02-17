package net.kapitencraft.scripted.edit.graphical.connector;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprWidget;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class ExprChainConnector extends ExprConnector {
    private final ExprWidget expr;

    public ExprChainConnector(int x, int y, ExprWidget widget) {
        super(x, y);
        expr = widget;
    }

    @Override
    public void insert(@Nullable CodeWidget widget) {
        this.expr.setChild((ExprWidget) widget);
    }

    @Override
    public CodeWidget get() {
        return this.expr.getChild();
    }

    @Override
    public void renderDebug(GuiGraphics graphics) {
        graphics.fill(x - 1, y - 5, x + 1, y + 15, 0x8F0000FF);
    }

    @Override
    public boolean canConnect(int xO, int yO, int xM, int yM) {
        return false;
    }
}
