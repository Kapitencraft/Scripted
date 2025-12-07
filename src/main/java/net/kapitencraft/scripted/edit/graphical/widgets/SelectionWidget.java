package net.kapitencraft.scripted.edit.graphical.widgets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectionWidget implements CodeWidget {
    public static final MapCodec<SelectionWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.listOf().fieldOf("entries").forGetter(w -> w.entries)
    ).apply(i, SelectionWidget::new));

    private final List<String> entries;
    private int index;

    public SelectionWidget(List<String> entries) {
        this.entries = entries;
    }

    private SelectionWidget(List<String> entries, int index) {
        this(entries);
        this.index = index;
    }

    @Override
    public CodeWidget copy() {
        return new SelectionWidget(
                this.entries,
                this.index
        );
    }

    @Override
    public @NotNull Type getType() {
        return Type.SELECTION;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {

    }

    @Override
    public int getWidth(Font font) {
        return font.width(entries.get(index)) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }
}
