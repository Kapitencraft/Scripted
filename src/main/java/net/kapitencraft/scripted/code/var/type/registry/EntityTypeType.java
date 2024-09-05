package net.kapitencraft.scripted.code.var.type.registry;

import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityTypeType extends RegistryType<EntityType<?>> {
    public EntityTypeType() {
        super("EntityType", ForgeRegistries.ENTITY_TYPES);
    }

    @Override
    public Class<EntityType<?>> getTypeClass() {
        return null;
    }
}
