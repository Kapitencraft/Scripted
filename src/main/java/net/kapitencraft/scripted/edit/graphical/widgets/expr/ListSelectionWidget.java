package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.inserter.GhostInserter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ListSelectionWidget implements ExprCodeWidget {
    public static final MapCodec<ListSelectionWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.listOf().fieldOf("entries").forGetter(w -> w.entries)
    ).apply(i, ListSelectionWidget::new));

    private final List<String> entries;
    private int index;

    public ListSelectionWidget(List<String> entries) {
        this.entries = entries;
    }

    private ListSelectionWidget(List<String> entries, int index) {
        this(entries);
        this.index = index;
    }

    @Override
    public ExprCodeWidget copy() {
        return new ListSelectionWidget(
                this.entries,
                this.index
        );
    }

    @Override
    public GhostInserter getGhostWidgetTarget(int x, int y, Font font, boolean isBlock) {
        return null;
    }

    @Override
    public void update(@Nullable MethodContext context) {

    }

    @Override
    public @NotNull Type getType() {
        return Type.LIST_SELECTION;
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
