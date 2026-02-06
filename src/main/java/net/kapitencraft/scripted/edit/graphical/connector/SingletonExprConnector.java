package net.kapitencraft.scripted.edit.graphical.connector;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SingletonExprConnector extends ExprConnector {
    private final Consumer<ExprCodeWidget> inserter;
    private final Supplier<ExprCodeWidget> getter;

    public SingletonExprConnector(int x, int y, Consumer<ExprCodeWidget> inserter, Supplier<ExprCodeWidget> getter) {
        super(x, y);
        this.inserter = inserter;
        this.getter = getter;
    }

    @Override
    public void insert(@Nullable CodeWidget widget) {
        this.inserter.accept((ExprCodeWidget) widget);
    }

    @Override
    public CodeWidget get() {
        return this.getter.get();
    }
}
