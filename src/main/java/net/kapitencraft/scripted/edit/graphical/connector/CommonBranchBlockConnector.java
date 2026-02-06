package net.kapitencraft.scripted.edit.graphical.connector;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommonBranchBlockConnector extends BlockConnector {
    private final Consumer<BlockCodeWidget> inserter;
    private final Supplier<BlockCodeWidget> getter;

    public CommonBranchBlockConnector(int x, int y, Consumer<BlockCodeWidget> inserter, Supplier<BlockCodeWidget> getter, Font font, Consumer<Connector> collector) {
        super(x, y);
        BlockCodeWidget v;
        if ((v = getter.get()) != null) {
            v.collectConnectors(x, y, font, collector);
        }
        this.inserter = inserter;
        this.getter = getter;
    }

    @Override
    public void insert(@Nullable CodeWidget widget) {
        this.inserter.accept((BlockCodeWidget) widget);
    }

    @Override
    public BlockCodeWidget get() {
        return getter.get();
    }
}
