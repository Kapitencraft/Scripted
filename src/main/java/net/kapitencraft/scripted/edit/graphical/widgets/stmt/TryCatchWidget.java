package net.kapitencraft.scripted.edit.graphical.widgets.stmt;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

//TODO
public class TryCatchWidget extends StmtCodeWidget {

    private StmtCodeWidget body;

    @Override
    protected @NotNull Type getType() {
        return null;
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public StmtCodeWidget copy() {
        return new TryCatchWidget();
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {

        super.render(graphics, font, renderX, renderY);
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {

    }

    @Override
    public CodeWidget getByName(String arg) {
        return null;
    }

    public static class CatchBranch {


        private ClassReference type;
        private StmtCodeWidget body;

    }
}
