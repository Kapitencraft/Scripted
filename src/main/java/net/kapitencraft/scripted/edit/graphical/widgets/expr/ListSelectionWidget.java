package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * these should never be serialized. store all other information on the overlying expr or statement
 */
public class ListSelectionWidget<T> implements ExprCodeWidget {

    private final List<T> entries;
    private final Function<T, String> textProvider;
    private int index;

    public ListSelectionWidget(List<T> entries, Function<T, String> textProvider) {
        this.entries = entries;
        this.textProvider = textProvider;
    }

    private ListSelectionWidget(List<T> entries, Function<T, String> textProvider, int index) {
        this(entries, textProvider);
        this.index = index;
    }

    @Override
    public ExprCodeWidget copy() {
        return new ListSelectionWidget<>(
                this.entries, this.textProvider,
                this.index);
    }

    @Override
    public GhostInserter getGhostWidgetTarget(int x, int y, Font font, boolean isBlock) {
        return null;
    }

    @Override
    public void update(@Nullable MethodContext context) {

    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public @NotNull Type getType() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {

    }

    @Override
    public int getWidth(Font font) {
        return font.width(textProvider.apply(entries.get(index))) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }

    public T getValue() {
        return this.entries.isEmpty() ? null : this.entries.get(index);
    }
}
