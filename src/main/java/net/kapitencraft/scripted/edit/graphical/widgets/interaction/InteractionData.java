package net.kapitencraft.scripted.edit.graphical.widgets.interaction;

import net.kapitencraft.kap_lib.core.client.widget.PositionedWidget;
import net.minecraft.client.gui.Font;

import java.util.function.Consumer;

public class InteractionData {
    private final Consumer<PositionedWidget> sink;
    private final Font font;
    private final int width, height;

    public InteractionData(Consumer<PositionedWidget> sink, Font font, int width, int height) {
        this.sink = sink;
        this.font = font;
        this.width = width;
        this.height = height;
    }

    public void openWidget(PositionedWidget constructor) {
        this.sink.accept(constructor);
    }

    public Font getFont() {
        return font;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * call on each selection widget to automatically close the widget when a selection has been made
     */
    public <T> Consumer<T> wrapCloseWidget(Consumer<T> in) {
        return t -> {
            in.accept(t);
            this.sink.accept(null);
        };
    }

    public Runnable closeWidget() {
        return () -> this.sink.accept(null);
    }
}
