package net.kapitencraft.scripted.init.custom;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.script.type.ScriptType;
import net.kapitencraft.scripted.code.var.VarType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface ModRegistryKeys {
    ResourceKey<Registry<VarType<?>>> VAR_TYPES = makeKey("var_types");
    ResourceKey<Registry<Function>> FUNCTIONS = makeKey("functions");
    ResourceKey<Registry<ScriptType<?, ?>>> SCRIPT_TYPES = makeKey("script_types");
    ResourceKey<Registry<Method<?>>> METHODS = makeKey("methods");

    private static <T> ResourceKey<Registry<T>> makeKey(String key) {
        return ResourceKey.createRegistryKey(Scripted.res(key));
    }
}
