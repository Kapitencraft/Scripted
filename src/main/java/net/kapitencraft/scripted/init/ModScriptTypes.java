package net.kapitencraft.scripted.init;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.script.type.ScriptType;
import net.kapitencraft.scripted.code.script.type.SimpleScript;
import net.kapitencraft.scripted.init.custom.ModRegistryKeys;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface ModScriptTypes {
    DeferredRegister<ScriptType<?, ?>> REGISTRY = Scripted.createRegistry(ModRegistryKeys.SCRIPT_TYPES);

    RegistryObject<SimpleScript> SIMPLE = REGISTRY.register("simple", SimpleScript::new);
}
