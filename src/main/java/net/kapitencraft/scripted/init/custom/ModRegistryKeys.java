package net.kapitencraft.scripted.init.custom;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.method.MethodCall;
import net.kapitencraft.scripted.code.vars.VarType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class ModRegistryKeys {
    public static final ResourceKey<Registry<MethodCall>> METHODS = createRegistry("methods");
    public static final ResourceKey<Registry<VarType<?>>> VAR_TYPES = createRegistry("var_types");
    private static <T> ResourceKey<Registry<T>> createRegistry(String id) {
        return ResourceKey.createRegistryKey(Scripted.res(id));
    }
}
