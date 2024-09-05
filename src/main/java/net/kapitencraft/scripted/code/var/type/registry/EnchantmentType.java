package net.kapitencraft.scripted.code.var.type.registry;

import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantmentType extends RegistryType<Enchantment> {
    public EnchantmentType() {
        super("Enchantment", ForgeRegistries.ENCHANTMENTS);
    }

    @Override
    public Class<Enchantment> getTypeClass() {
        return Enchantment.class;
    }
}
