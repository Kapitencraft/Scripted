package net.kapitencraft.scripted.code.var.type.registry;

import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

public class MobEffectType extends RegistryType<MobEffect> {
    public MobEffectType() {
        super("MobEffect", ForgeRegistries.MOB_EFFECTS);
    }
}
