package net.kapitencraft.scripted.init;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.builtin.WhenMethod;
import net.kapitencraft.scripted.code.exe.methods.builtin.primitive.BooleanOperationMethod;
import net.kapitencraft.scripted.code.exe.methods.builtin.primitive.MathOperationMethod;
import net.kapitencraft.scripted.code.exe.methods.builtin.primitive.NotMethod;
import net.kapitencraft.scripted.code.exe.methods.mapper.FieldReference;
import net.kapitencraft.scripted.code.exe.methods.mapper.VarReference;
import net.kapitencraft.scripted.init.custom.ModRegistryKeys;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public interface ModMethods {
    DeferredRegister<Method<?>> REGISTRY = Scripted.createRegistry(ModRegistryKeys.METHODS);

    //reference
    RegistryObject<VarReference<?>> VAR_REFERENCE = REGISTRY.register("var", VarReference::new);
    RegistryObject<FieldReference<?, ?>> FIELD_REFERENCE = REGISTRY.register("field", FieldReference::new);
    //math operations
    RegistryObject<MathOperationMethod<?>> ADDITION = REGISTRY.register("add", MathOperationMethod::add);
    RegistryObject<MathOperationMethod<?>> MULTIPLICATION = REGISTRY.register("mul", MathOperationMethod::mul);
    RegistryObject<MathOperationMethod<?>> DIVISION = REGISTRY.register("div", MathOperationMethod::div);
    RegistryObject<MathOperationMethod<?>> SUBTRACTION = REGISTRY.register("sub", MathOperationMethod::sub);
    //boolean operations
    RegistryObject<BooleanOperationMethod> AND = REGISTRY.register("and", BooleanOperationMethod::and);
    RegistryObject<BooleanOperationMethod> OR = REGISTRY.register("or", BooleanOperationMethod::or);
    RegistryObject<BooleanOperationMethod> XOR = REGISTRY.register("xor", BooleanOperationMethod::xor);
    RegistryObject<NotMethod> NOT = REGISTRY.register("not", NotMethod::new);
    //other
    RegistryObject<WhenMethod<?>> WHEN = REGISTRY.register("when", WhenMethod::new);

}