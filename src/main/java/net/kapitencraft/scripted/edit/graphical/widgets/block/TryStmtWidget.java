package net.kapitencraft.scripted.edit.graphical.widgets.block;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.NotNull;

public class TryStmtWidget extends BlockCodeWidget {

    private BlockCodeWidget body;

    @Override
    protected @NotNull Type getType() {
        return Type.TRY_STMT;
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
    public BlockCodeWidget copy() {
        return null;
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {

    }

    @Override
    public CodeWidget getByName(String argName) {
        return null;
    }
}
