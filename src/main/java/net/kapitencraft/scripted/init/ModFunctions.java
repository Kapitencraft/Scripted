package net.kapitencraft.scripted.init;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.functions.builtin.*;
import net.kapitencraft.scripted.init.custom.ModRegistryKeys;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface ModFunctions {
    DeferredRegister<Function> REGISTRY = Scripted.createRegistry(ModRegistryKeys.FUNCTIONS);

    RegistryObject<CreateVarFunction> CREATE_VAR = REGISTRY.register("create_var", CreateVarFunction::new);
    RegistryObject<SetVarFunction> SET_VAR = REGISTRY.register("set_var", SetVarFunction::new);
    RegistryObject<CreateAndSetVarFunction> CREATE_AND_SET_VAR = REGISTRY.register("create_and_set_var", CreateAndSetVarFunction::new);
    RegistryObject<IfFunction> IF = REGISTRY.register("if", IfFunction::new);
    RegistryObject<ElIfFunction> ELSE_IF = REGISTRY.register("else_if", ElIfFunction::new);
    RegistryObject<ElseFunction> ELSE = REGISTRY.register("else", ElseFunction::new);
    RegistryObject<ReturnFunction> RETURN = REGISTRY.register("return", ReturnFunction::new);
    RegistryObject<WhileLoopFunction> WHILE = REGISTRY.register("while", WhileLoopFunction::new);
    RegistryObject<BreakFunction> BREAK = REGISTRY.register("break", BreakFunction::new);
    RegistryObject<ContinueFunction> CONTINUE = REGISTRY.register("continue", ContinueFunction::new);
    RegistryObject<ForLoopFunction> FOR_LOOP = REGISTRY.register("for_loop", ForLoopFunction::new);
}
