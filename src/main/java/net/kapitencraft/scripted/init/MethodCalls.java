package net.kapitencraft.scripted.init;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.method.MethodCall;
import net.kapitencraft.scripted.code.method.elements.CreateAndSetVarMethod;
import net.kapitencraft.scripted.code.method.elements.CreateVarMethod;
import net.kapitencraft.scripted.code.method.elements.SetVarMethod;
import net.kapitencraft.scripted.init.custom.ModRegistryKeys;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface MethodCalls {
    DeferredRegister<MethodCall> REGISTRY = Scripted.createRegistry(ModRegistryKeys.METHODS);

    RegistryObject<CreateVarMethod> CREATE_VAR = REGISTRY.register("create_var", CreateVarMethod::new);
    RegistryObject<SetVarMethod> SET_VAR = REGISTRY.register("set_var", SetVarMethod::new);
    RegistryObject<CreateAndSetVarMethod> CREATE_AND_SET_VAR = REGISTRY.register("create_and_set_var", CreateAndSetVarMethod::new);
}