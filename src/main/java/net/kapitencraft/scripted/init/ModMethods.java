package net.kapitencraft.scripted.init;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.builtin.primitive.BooleanOperationMethod;
import net.kapitencraft.scripted.code.exe.methods.builtin.primitive.NotMethod;
import net.kapitencraft.scripted.code.exe.methods.mapper.VarReference;
import net.kapitencraft.scripted.init.custom.ModRegistryKeys;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface ModMethods {
    DeferredRegister<Method<?>> REGISTRY = Scripted.createRegistry(ModRegistryKeys.METHODS);

    //reference
    RegistryObject<VarReference<?>> VAR_REFERENCE = REGISTRY.register("var", VarReference::new);
    //boolean operations
    RegistryObject<BooleanOperationMethod> BOOL_OPERATION = REGISTRY.register("bool_operation", BooleanOperationMethod::new);
    RegistryObject<NotMethod> NOT = REGISTRY.register("not", NotMethod::new);
}