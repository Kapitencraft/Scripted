package net.kapitencraft.scripted.init.custom;

import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.script.type.ScriptType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public interface ModRegistries {
    IForgeRegistry<VarType<?>> VAR_TYPES = RegistryManager.ACTIVE.getRegistry(ModRegistryKeys.VAR_TYPES);
    IForgeRegistry<Function> FUNCTIONS = RegistryManager.ACTIVE.getRegistry(ModRegistryKeys.FUNCTIONS);
    IForgeRegistry<ScriptType<?, ?>> SCRIPT_TYPES = RegistryManager.ACTIVE.getRegistry(ModRegistryKeys.SCRIPT_TYPES);
    IForgeRegistry<Method<?>> METHODS = RegistryManager.ACTIVE.getRegistry(ModRegistryKeys.METHODS);
}