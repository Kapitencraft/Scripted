package net.kapitencraft.scripted.init.custom;

import net.kapitencraft.scripted.code.method.MethodCall;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public interface ModRegistries {
    ForgeRegistry<MethodCall> METHODS = RegistryManager.ACTIVE.getRegistry(ModRegistryKeys.METHODS.registry());
}
