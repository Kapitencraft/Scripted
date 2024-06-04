package net.kapitencraft.scripted.init.custom;

import net.kapitencraft.scripted.code.method.elements.abstracts.Function;
import net.kapitencraft.scripted.code.script.type.ScriptType;
import net.kapitencraft.scripted.code.var.VarType;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public interface ModRegistries {
    IForgeRegistry<VarType<?>> VAR_TYPES = RegistryManager.ACTIVE.getRegistry(ModRegistryKeys.VAR_TYPES);
    IForgeRegistry<Function> FUNCTIONS = RegistryManager.ACTIVE.getRegistry(ModRegistryKeys.FUNCTIONS);
    IForgeRegistry<ScriptType> SCRIPT_TYPES = RegistryManager.ACTIVE.getRegistry(ModRegistryKeys.SCRIPT_TYPES);
}