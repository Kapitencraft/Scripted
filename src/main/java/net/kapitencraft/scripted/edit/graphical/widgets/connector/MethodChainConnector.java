package net.kapitencraft.scripted.edit.graphical.widgets.connector;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.MethodWidget;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class MethodChainConnector extends ExprConnector {
    private final MethodWidget expr;

    public MethodChainConnector(int x, int y, MethodWidget widget) {
        super(x, y);
        expr = widget;
    }

    @Override
    public void insert(@Nullable CodeWidget widget) {
        this.expr.setChild((ExprCodeWidget) widget);
    }

    @Override
    public CodeWidget get() {
        return this.expr.getChild();
    }

    @Override
    public void renderDebug(GuiGraphics graphics) {
        graphics.fill(x - 1, y - 5, x + 1, y + 15, 0x8F0000FF);
    }
}
