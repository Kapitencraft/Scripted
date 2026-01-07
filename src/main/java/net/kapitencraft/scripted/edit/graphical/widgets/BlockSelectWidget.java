package net.kapitencraft.scripted.edit.graphical.widgets;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.kap_lib.client.UsefulTextures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockSelectWidget implements ExprCodeWidget {
    public static final MapCodec<BlockSelectWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block", Blocks.AIR).forGetter(w -> w.value)
    ).apply(i, BlockSelectWidget::new));

    private Block value = Blocks.AIR;
    private ItemStack stack = ItemStack.EMPTY;

    public BlockSelectWidget(Block block) {
        this.setBlock(block);
    }

    public BlockSelectWidget() {
        this.setBlock(Blocks.STONE);
    }

    @Override
    public @NotNull Type getType() {
        return Type.SELECT_BLOCK;
    }

    public void setBlock(Block block) {
        if (this.value != block) {
            this.value = block;
            this.stack = new ItemStack(block.asItem());
        }
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        UsefulTextures.renderSlotBackground(graphics, renderX, renderY - 4);
        graphics.renderItem(this.stack, renderX, renderY - 4);
    }

    @Override
    public int getWidth(Font font) {
        return 19;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public ExprCodeWidget copy() {
        return new BlockSelectWidget(this.value);
    }

    @Override
    public @Nullable BlockWidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return null;
    }
}
