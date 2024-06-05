package net.kapitencraft.scripted.init;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.type.ItemType;
import net.kapitencraft.scripted.code.var.type.VarTypeType;
import net.kapitencraft.scripted.code.var.type.Vec3Type;
import net.kapitencraft.scripted.code.var.type.data.DataStorageType;
import net.kapitencraft.scripted.code.var.type.ItemStackType;
import net.kapitencraft.scripted.init.custom.ModRegistryKeys;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface ModVarTypes {
    DeferredRegister<VarType<?>> REGISTRY = Scripted.createRegistry(ModRegistryKeys.VAR_TYPES);

    RegistryObject<VarType<Integer>> INTEGER = REGISTRY.register("integer", () -> new VarType<Integer>(null, null, null, null));
    RegistryObject<VarType<Double>> DOUBLE = REGISTRY.register("double", () -> new VarType<Double>(null, null, null, null));
    RegistryObject<VarType<String>> STRING = REGISTRY.register("string", () -> new VarType<String>(null, null, null, null));
    RegistryObject<VarType<Character>> CHAR = REGISTRY.register("char", () -> new VarType<Character>(null, null, null, null));
    RegistryObject<VarType<Boolean>> BOOL = REGISTRY.register("bool", () -> new VarType<Boolean>(null, null, null, null));

    RegistryObject<Vec3Type> VEC3 = REGISTRY.register("vec3", Vec3Type::new);

    RegistryObject<ItemType> ITEM = REGISTRY.register("item", ItemType::new);
    RegistryObject<DataStorageType> DATA_STORAGE = REGISTRY.register("data_storage", DataStorageType::new);
    RegistryObject<ItemStackType> ITEM_STACK = REGISTRY.register("item_stack", ItemStackType::new);
    RegistryObject<VarTypeType> VAR_TYPE = REGISTRY.register("var_type", VarTypeType::new);
}