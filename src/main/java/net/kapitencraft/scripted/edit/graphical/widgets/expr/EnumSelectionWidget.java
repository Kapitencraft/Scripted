package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import net.kapitencraft.kap_lib.core.client.widget.select.SelectEnumWidget;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.InteractionData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * these should never be serialized. store all other information on the overlying expr or statement
 */
public class EnumSelectionWidget<T> implements ExprCodeWidget {

    private int x, y;
    private final List<T> entries;
    private final Function<T, String> textProvider;
    private int index;

    public EnumSelectionWidget(List<T> entries, Function<T, String> textProvider) {
        this.entries = entries;
        this.textProvider = textProvider;
    }

    private EnumSelectionWidget(List<T> entries, Function<T, String> textProvider, int index) {
        this(entries, textProvider);
        this.index = index;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }
    @Override
    public int getY() {
        return y;
    }

    @Override
    public ExprCodeWidget copy() {
        return new EnumSelectionWidget<>(
                this.entries, this.textProvider,
                this.index);
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        throw new IllegalAccessError("can not insert into list selector widget");
    }

    @Override
    public CodeWidget getByName(String arg) {
        throw new IllegalAccessError("can not get from var list selector widget");
    }

    @Override
    public void collectConnectors(Font font, Consumer<Connector> collector) {

    }

    @Override
    public void update(@Nullable MethodContext context, Font font, int x, int y) {
        this.x = x;
        this.y = y;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public @NotNull Type getType() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, Font font) {
        graphics.blitSprite(ExprCategory.OTHER.getSpriteLocation(), x, y, getWidth(font), 10);
        graphics.drawString(font, textProvider.apply(entries.get(index)), x + 2, y + 1, 0, false);
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
    public void registerInteractions(Font font, Consumer<CodeInteraction> sink) {
        sink.accept(new Interaction(x, y, getWidth(font), getHeight()));
    }

    private class Interaction extends CodeInteraction {

        protected Interaction(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void onClick(int mouseX, int mouseY, InteractionData data) {
            data.openWidget(new SelectEnumWidget<>(
                    50, 20,
                    data.getWidth() - 100,
                    data.getHeight() - 40,
                    data.getFont(),
                    entries,
                    v -> Component.literal(textProvider.apply(v)),
                    data.wrapCloseWidget(EnumSelectionWidget.this::set),
                    Component.literal("Select block")
            ));
        }
    }

    public T getValue() {
        return this.entries.isEmpty() ? null : this.entries.get(index);
    }

    public void set(T value) {
        int i = this.entries.indexOf(value);
        if (i != -1) {
            this.index = i;
        } else
            throw new IndexOutOfBoundsException("unknown value: " + value);
    }
}
