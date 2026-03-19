package net.kapitencraft.scripted.edit.graphical.connector;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.StmtCodeWidget;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommonBranchBlockConnector extends BlockConnector {
    private final Consumer<StmtCodeWidget> inserter;
    private final Supplier<StmtCodeWidget> getter;

    public CommonBranchBlockConnector(int x, int y, Consumer<StmtCodeWidget> inserter, Supplier<StmtCodeWidget> getter, Font font, Consumer<Connector> collector) {
        super(x, y);
        StmtCodeWidget v;
        if ((v = getter.get()) != null) {
            v.collectConnectors(x, y, font, collector);
        }
        this.inserter = inserter;
        this.getter = getter;
    }

    @Override
    public void insert(@Nullable CodeWidget widget) {
        this.inserter.accept((StmtCodeWidget) widget);
    }

    @Override
    public StmtCodeWidget get() {
        return getter.get();
    }
}
