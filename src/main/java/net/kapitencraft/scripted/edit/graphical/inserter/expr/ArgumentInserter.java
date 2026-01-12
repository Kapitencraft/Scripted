package net.kapitencraft.scripted.edit.graphical.inserter.expr;

import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.minecraft.client.gui.Font;

import java.util.function.Consumer;

public class ArgumentInserter implements ExprGhostInserter {
    private final Consumer<ExprCodeWidget> valueSink;

    public ArgumentInserter(Consumer<ExprCodeWidget> valueSink) {
        this.valueSink = valueSink;
    }

    public static GhostInserter createSpecific(int x, int y, Font font, String translation, String argName, Consumer<ExprCodeWidget> valueSink, ExprCodeWidget entry) {

        return new ArgumentInserter(valueSink);
    }

    @Override
    public void insert(CodeWidget target) {
        this.valueSink.accept((ExprCodeWidget) target);
    }
}
