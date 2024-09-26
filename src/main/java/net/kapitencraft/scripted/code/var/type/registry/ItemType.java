package net.kapitencraft.scripted.code.var.type.registry;

import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemType extends RegistryType<Item> {
    public ItemType() {
        super("Item", ForgeRegistries.ITEMS);
    }

    @Override
    public void render(int x, int y, Item value, GuiGraphics graphics) {
        graphics.renderFakeItem(new ItemStack(value), x, y);
    }

    @Override
    public Class<Item> getTypeClass() {
        return Item.class;
    }
}
