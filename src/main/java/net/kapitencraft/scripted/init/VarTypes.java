package net.kapitencraft.scripted.init;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.var.type.BlockStateType;
import net.kapitencraft.scripted.code.var.type.ItemStackType;
import net.kapitencraft.scripted.code.var.type.LevelType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.code.var.type.data.DataStorageType;
import net.kapitencraft.scripted.code.var.type.entity.EntityType;
import net.kapitencraft.scripted.code.var.type.entity.LivingEntityType;
import net.kapitencraft.scripted.code.var.type.math.BlockPosType;
import net.kapitencraft.scripted.code.var.type.math.Vec2Type;
import net.kapitencraft.scripted.code.var.type.math.Vec3Type;
import net.kapitencraft.scripted.code.var.type.primitive.*;
import net.kapitencraft.scripted.code.var.type.registry.AttributeType;
import net.kapitencraft.scripted.code.var.type.registry.BlockType;
import net.kapitencraft.scripted.code.var.type.registry.EntityTypeType;
import net.kapitencraft.scripted.code.var.type.registry.ItemType;
import net.kapitencraft.scripted.init.custom.ModRegistryKeys;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface VarTypes {
    DeferredRegister<VarType<?>> REGISTRY = Scripted.createRegistry(ModRegistryKeys.VAR_TYPES);

    //special
    RegistryObject<VarType<Void>> VOID = REGISTRY.register("void", VoidType::new);

    //primitives
    RegistryObject<VarType<Integer>> INTEGER = REGISTRY.register("integer", IntegerType::new);
    RegistryObject<VarType<Double>> DOUBLE = REGISTRY.register("double", DoubleType::new);
    RegistryObject<VarType<String>> STRING = REGISTRY.register("string", StringType::new);
    RegistryObject<VarType<Character>> CHAR = REGISTRY.register("char", CharType::new);
    RegistryObject<VarType<Boolean>> BOOL = REGISTRY.register("bool", BooleanType::new);

    //math-helper
    RegistryObject<Vec3Type> VEC3 = REGISTRY.register("vec3", Vec3Type::new);
    RegistryObject<Vec2Type> VEC2 = REGISTRY.register("vec2", Vec2Type::new);
    RegistryObject<BlockPosType> BLOCK_POS = REGISTRY.register("block_pos", BlockPosType::new);

    //data
    RegistryObject<DataStorageType> DATA_STORAGE = REGISTRY.register("data_storage", DataStorageType::new);


    //registry
    RegistryObject<ItemType> ITEM = REGISTRY.register("item", ItemType::new);
    RegistryObject<BlockType> BLOCK = REGISTRY.register("block", BlockType::new);
    RegistryObject<ItemStackType> ITEM_STACK = REGISTRY.register("item_stack", ItemStackType::new);
    RegistryObject<EntityTypeType> ENTITY_TYPE = REGISTRY.register("entity_type", EntityTypeType::new);
    RegistryObject<AttributeType> ATTRIBUTE = REGISTRY.register("attribute", AttributeType::new);

    //entity
    RegistryObject<EntityType<Entity>> ENTITY = REGISTRY.register("entity", () -> new EntityType<>("Entity"));
    RegistryObject<LivingEntityType<LivingEntity>> LIVING_ENTITY = REGISTRY.register("living_entity", ()-> new LivingEntityType<>("LivingEntity"));

    //other
    RegistryObject<LevelType> LEVEL = REGISTRY.register("level", LevelType::new);
    RegistryObject<BlockStateType> BLOCK_STATE = REGISTRY.register("block_state", BlockStateType::new);
    //RegistryObject<BlockStatePropertyType<?>> BLOCK_STATE_PROPERTY = REGISTRY.register("block_state_property", BlockStatePropertyType::new); //TODO make generic var types
}