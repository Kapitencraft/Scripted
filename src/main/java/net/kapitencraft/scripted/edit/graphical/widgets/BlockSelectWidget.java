package net.kapitencraft.scripted.edit.graphical.widgets;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockSelectWidget implements CodeWidget {
    public static final MapCodec<BlockSelectWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block", Blocks.AIR).forGetter(w -> w.value)
    ).apply(i, BlockSelectWidget::new));

    private Block value = Blocks.AIR;

    public BlockSelectWidget(Block block) {
        this.value = block;
    }

    public BlockSelectWidget() {
    }

    @Override
    public @NotNull Type getType() {
        return Type.SELECT_BLOCK;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
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
    public CodeWidget copy() {
        return null;
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }
}
