package net.kapitencraft.scripted.code.var.type;

import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class ItemType extends RegistryType<Item> {
    public ItemType() {
        super(ForgeRegistries.ITEMS);
    }

    @Override
    public void render(int x, int y, Item value, GuiGraphics graphics) {
        graphics.renderFakeItem(new ItemStack(value), x, y);
    }
}
