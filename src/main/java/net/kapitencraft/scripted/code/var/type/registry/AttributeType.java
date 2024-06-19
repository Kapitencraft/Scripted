package net.kapitencraft.scripted.code.var.type.registry;

import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

public class AttributeType extends RegistryType<Attribute> {
    public AttributeType() {
        super("Attribute", ForgeRegistries.ATTRIBUTES);
    }
}
