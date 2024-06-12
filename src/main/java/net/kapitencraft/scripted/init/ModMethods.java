package net.kapitencraft.scripted.init;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.builtin.WhenMethod;
import net.kapitencraft.scripted.code.exe.methods.builtin.primitive.BooleanOperationMethod;
import net.kapitencraft.scripted.code.exe.methods.builtin.primitive.Comparators;
import net.kapitencraft.scripted.code.exe.methods.builtin.primitive.MathOperationMethod;
import net.kapitencraft.scripted.code.exe.methods.builtin.primitive.NotMethod;
import net.kapitencraft.scripted.code.exe.methods.mapper.FieldReference;
import net.kapitencraft.scripted.code.exe.methods.mapper.PrimitiveReference;
import net.kapitencraft.scripted.code.exe.methods.mapper.VarReference;
import net.kapitencraft.scripted.init.custom.ModRegistryKeys;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface ModMethods {
    DeferredRegister<Method<?>> REGISTRY = Scripted.createRegistry(ModRegistryKeys.METHODS);

    //reference
    RegistryObject<VarReference<?>> VAR_REFERENCE = REGISTRY.register("var", VarReference::new);
    RegistryObject<FieldReference<?, ?>> FIELD_REFERENCE = REGISTRY.register("field", FieldReference::new);
    RegistryObject<PrimitiveReference<?>> PRIMITIVE = REGISTRY.register("primitive", PrimitiveReference::new);
    //math operations
    RegistryObject<MathOperationMethod<?>> ADDITION = REGISTRY.register("math_operation", MathOperationMethod::new);
    //boolean operations
    RegistryObject<BooleanOperationMethod> BOOL_OPERATION = REGISTRY.register("bool_operation", BooleanOperationMethod::new);
    RegistryObject<NotMethod> NOT = REGISTRY.register("not", NotMethod::new);
    RegistryObject<Comparators<?>> COMPARATORS = REGISTRY.register("comparators", Comparators::new);
    //other
    RegistryObject<WhenMethod<?>> WHEN = REGISTRY.register("when", WhenMethod::new);

}