package net.kapitencraft.scripted.init;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.vars.VarType;
import net.kapitencraft.scripted.init.custom.ModRegistryKeys;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public interface VarTypes {
    DeferredRegister<VarType<?>> REGISTRY = Scripted.createRegistry(ModRegistryKeys.VAR_TYPES);

    RegistryObject<VarType<String>> STRING = REGISTRY.register("string", VarType::new);
    RegistryObject<VarType<VarType<?>>> TYPE = REGISTRY.register("type", VarType::new);
    /**
     * used when adding something via Type References <i>Use with precaution!</i>
     */
    RegistryObject<VarType<?>> WILDCARD = REGISTRY.register("wildcard", VarType::new);
}