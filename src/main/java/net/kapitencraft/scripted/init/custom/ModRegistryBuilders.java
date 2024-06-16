package net.kapitencraft.scripted.init.custom;

import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.script.type.ScriptType;
import net.kapitencraft.scripted.code.var.VarType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegistryBuilder;

public interface ModRegistryBuilders {
    RegistryBuilder<VarType<?>> VAR_TYPE_BUILDER = create(ModRegistryKeys.VAR_TYPES).addCallback(new ModCallbacks.Types());
    RegistryBuilder<Function> FUNCTION_BUILDER = create(ModRegistryKeys.FUNCTIONS).addCallback(new ModCallbacks.Functions());
    RegistryBuilder<Method<?>> METHODS = create(ModRegistryKeys.METHODS).addCallback(new ModCallbacks.Methods());
    RegistryBuilder<ScriptType<?, ?>> SCRIPT_TYPES = create(ModRegistryKeys.SCRIPT_TYPES);

    private static <T> RegistryBuilder<T> create(ResourceKey<Registry<T>> key) {
        return new RegistryBuilder<T>().setName(key.location());
    }
}
