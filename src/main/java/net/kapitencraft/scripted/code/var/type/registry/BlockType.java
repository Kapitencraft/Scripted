package net.kapitencraft.scripted.code.var.type.registry;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockType extends RegistryType<Block> {
    protected BlockType() {
        super("Block", ForgeRegistries.BLOCKS);
    }

    @Override
    public void render(int x, int y, Block value, GuiGraphics graphics) {
        graphics.renderFakeItem(new ItemStack(value.asItem()), x, y);
    }

    @Override
    public void saveToJson(JsonObject object, Block value) {

    }

    @Override
    public Block loadFromJson(JsonObject object) {
        return null;
    }
}
