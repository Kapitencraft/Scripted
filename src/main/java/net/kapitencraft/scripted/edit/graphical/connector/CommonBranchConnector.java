package net.kapitencraft.scripted.edit.graphical.connector;

import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommonBranchConnector extends Connector {
    private final Consumer<BlockCodeWidget> inserter;
    private final Supplier<BlockCodeWidget> getter;

    public CommonBranchConnector(int x, int y, Consumer<BlockCodeWidget> inserter, Supplier<BlockCodeWidget> getter, Consumer<Connector> collector) {
        super(x, y);
        BlockCodeWidget v;
        if ((v = getter.get()) != null) {
            v.collectConnectors(x, y, collector);
        }
        this.inserter = inserter;
        this.getter = getter;
    }

    @Override
    public void insert(BlockCodeWidget widget) {
        this.inserter.accept(widget);
    }

    @Override
    public BlockCodeWidget get() {
        return getter.get();
    }
}
