package net.kapitencraft.scripted.init.custom;

import net.kapitencraft.scripted.code.method.elements.abstracts.Function;
import net.kapitencraft.scripted.code.script.type.ScriptType;
import net.kapitencraft.scripted.code.var.VarType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegistryBuilder;

public interface ModRegistryBuilders {
    RegistryBuilder<VarType<?>> VAR_TYPE_BUILDER = create(ModRegistryKeys.VAR_TYPES);
    RegistryBuilder<Function> FUNCTION_BUILDER = create(ModRegistryKeys.FUNCTIONS);
    RegistryBuilder<ScriptType> SCRIPT_TYPES = create(ModRegistryKeys.SCRIPT_TYPES);

    private static <T> RegistryBuilder<T> create(ResourceKey<Registry<T>> key) {
        return new RegistryBuilder<T>().setName(key.location());
    }
}
