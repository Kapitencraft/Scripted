package net.kapitencraft.scripted.init;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.script.type.ScriptType;
import net.kapitencraft.scripted.init.custom.ModRegistryKeys;
import net.minecraftforge.registries.DeferredRegister;

public interface ModScriptTypes {
    DeferredRegister<ScriptType> REGISTRY = Scripted.createRegistry(ModRegistryKeys.SCRIPT_TYPES);
}
