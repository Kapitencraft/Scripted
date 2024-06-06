package net.kapitencraft.scripted.init;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.type.math.Vec2Type;
import net.kapitencraft.scripted.code.var.type.registry.ItemType;
import net.kapitencraft.scripted.code.var.type.registry.VarTypeType;
import net.kapitencraft.scripted.code.var.type.math.Vec3Type;
import net.kapitencraft.scripted.code.var.type.data.DataStorageType;
import net.kapitencraft.scripted.code.var.type.ItemStackType;
import net.kapitencraft.scripted.code.var.type.primitive.*;
import net.kapitencraft.scripted.init.custom.ModRegistryKeys;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface ModVarTypes {
    DeferredRegister<VarType<?>> REGISTRY = Scripted.createRegistry(ModRegistryKeys.VAR_TYPES);


    //primitives
    RegistryObject<VarType<Integer>> INTEGER = REGISTRY.register("integer", IntegerType::new);
    RegistryObject<VarType<Double>> DOUBLE = REGISTRY.register("double", DoubleType::new);
    RegistryObject<VarType<String>> STRING = REGISTRY.register("string", StringType::new);
    RegistryObject<VarType<Character>> CHAR = REGISTRY.register("char", CharType::new);
    RegistryObject<VarType<Boolean>> BOOL = REGISTRY.register("bool", BooleanType::new);

    //math-helper
    RegistryObject<Vec3Type> VEC3 = REGISTRY.register("vec3", Vec3Type::new);
    RegistryObject<Vec2Type> VEC2 = REGISTRY.register("vec2", Vec2Type::new);

    //data
    RegistryObject<DataStorageType> DATA_STORAGE = REGISTRY.register("data_storage", DataStorageType::new);


    //registry
    RegistryObject<ItemType> ITEM = REGISTRY.register("item", ItemType::new);
    RegistryObject<ItemStackType> ITEM_STACK = REGISTRY.register("item_stack", ItemStackType::new);
    RegistryObject<VarTypeType> VAR_TYPE = REGISTRY.register("var_type", VarTypeType::new);
}