package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
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
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        throw new IllegalAccessError("can not insert into list selector widget");
    }

    @Override
    public CodeWidget getByName(String argName) {
        throw new IllegalAccessError("can not get from var list selector widget");
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {

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
        graphics.blitSprite(ExprCategory.OTHER.getSpriteLocation(), renderX, renderY, getWidth(font), 10);
        graphics.drawString(font, textProvider.apply(entries.get(index)), renderX + 2, renderY + 1, -1);
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

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {

    }

    public T getValue() {
        return this.entries.isEmpty() ? null : this.entries.get(index);
    }
}
