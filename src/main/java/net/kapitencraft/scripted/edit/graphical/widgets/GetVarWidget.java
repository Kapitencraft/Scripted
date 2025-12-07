package net.kapitencraft.scripted.edit.graphical.widgets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GetVarWidget implements CodeWidget {
    public static final MapCodec<GetVarWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(w -> w.name)
    ).apply(i, GetVarWidget::new));

    private final String name;

    public GetVarWidget(String name) {
        this.name = name;
    }

    @Override
    public @NotNull Type getType() {
        return Type.GET_VAR;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {

    }

    @Override
    public CodeWidget copy() {
        return new GetVarWidget(this.name);
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }
}
