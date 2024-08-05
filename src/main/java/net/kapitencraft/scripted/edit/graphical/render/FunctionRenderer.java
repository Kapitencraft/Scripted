package net.kapitencraft.scripted.edit.graphical.render;

import net.kapitencraft.scripted.edit.graphical.Renderables;
import net.minecraft.client.gui.GuiGraphics;

public class FunctionRenderer extends IRenderer {
    private final boolean start, cancel;

    private FunctionRenderer child;
    private final MethodRenderer instance;

    public FunctionRenderer(boolean start, boolean cancel, MethodRenderer instance) {
        this.start = start;
        this.cancel = cancel;
        this.instance = instance;
    }

    public void setChild(FunctionRenderer child) {
        this.child = child;
    }


    @Override
    void render(GuiGraphics graphics) {
        Renderables.renderBodyStart(0xFFFFFFFF, 0, 0, graphics, start, cancel, 2);
    }
}
