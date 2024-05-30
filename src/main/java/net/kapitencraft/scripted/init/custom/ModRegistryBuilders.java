package net.kapitencraft.scripted.init.custom;

import net.kapitencraft.scripted.code.method.MethodCall;
import net.kapitencraft.scripted.code.vars.VarType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegistryBuilder;

public interface ModRegistryBuilders {
    RegistryBuilder<? extends MethodCall> METHODS_BUILDER = makeBuilder(ModRegistryKeys.METHODS);
    RegistryBuilder<VarType<?>> VAR_TYPE_BUILDER = makeBuilder(ModRegistryKeys.VAR_TYPES);
    private static <T> RegistryBuilder<T> makeBuilder(ResourceKey<Registry<T>> location) {
        return new RegistryBuilder<T>().setName(location.location());
    }
}
