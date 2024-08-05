package net.kapitencraft.scripted.init.custom;

import com.google.common.collect.Maps;
import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.functions.abstracts.SpecialFunction;
import net.kapitencraft.scripted.code.exe.methods.ISpecialMethod;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ModCallbacks {
    public static class Types implements IForgeRegistry.AddCallback<VarType<?>>, IForgeRegistry.CreateCallback<VarType<?>> {
        public static final ResourceLocation NAME_MAP = Scripted.res("name_map");
        public static final ResourceLocation PRIMITIVES = Scripted.res("primitives");
        public static final ResourceLocation REGISTRIES = Scripted.res("registries");

        @Override
        public void onAdd(IForgeRegistryInternal<VarType<?>> owner, RegistryManager stage, int id, ResourceKey<VarType<?>> key, VarType<?> obj, @Nullable VarType<?> oldObj) {
            if (oldObj == null) {
                obj.generate();
            }
            Map<String, VarType<?>> nameMap = owner.getSlaveMap(NAME_MAP, Map.class);
            if (oldObj != null) removeOldName(oldObj, nameMap);
            nameMap.put(obj.getName(), obj);
            List<PrimitiveType<?>> primitives = owner.getSlaveMap(PRIMITIVES, List.class);
            if (oldObj instanceof PrimitiveType<?>) primitives.remove(oldObj);
            if (obj instanceof PrimitiveType<?> prim) {
                primitives.add(prim);
            }

            Map<String, RegistryType<?>> registriesMap = owner.getSlaveMap(REGISTRIES, Map.class);
            if (oldObj instanceof RegistryType<?> registryType) {
                removeOldRegistry(registryType, registriesMap);
            }
            if (obj instanceof RegistryType<?> registryType) {
                registriesMap.put(registryType.getRegKey(), registryType);
            }
        }

        private static void removeOldName(VarType<?> type, Map<String, VarType<?>> nameMap) {
            nameMap.remove(type.getName());
        }

        private static void removeOldRegistry(RegistryType<?> regType, Map<String, RegistryType<?>> map) {
            map.remove(regType.getRegKey());
        }

        @Override
        public void onCreate(IForgeRegistryInternal<VarType<?>> owner, RegistryManager stage) {
            owner.setSlaveMap(NAME_MAP, Maps.newHashMap());
            owner.setSlaveMap(PRIMITIVES, Lists.newArrayList());
            owner.setSlaveMap(REGISTRIES, Maps.newHashMap());
        }
    }

    public static final ResourceLocation SPECIAL_MAP = Scripted.res("special");
    public static class Methods implements IForgeRegistry.AddCallback<Method<?>>, IForgeRegistry.CreateCallback<Method<?>> {

        @Override
        public void onAdd(IForgeRegistryInternal<Method<?>> owner, RegistryManager stage, int id, ResourceKey<Method<?>> key, Method<?> obj, @Nullable Method<?> oldObj) {
            if (oldObj instanceof ISpecialMethod<?> oldSpecial && obj instanceof ISpecialMethod<?> newSpecial) {
                List<ISpecialMethod<?>> specials = owner.getSlaveMap(SPECIAL_MAP, List.class);
                specials.remove(oldSpecial);
                specials.add(newSpecial);
            }
        }

        @Override
        public void onCreate(IForgeRegistryInternal<Method<?>> owner, RegistryManager stage) {
            owner.setSlaveMap(SPECIAL_MAP, Lists.newArrayList());
        }
    }

    public static class Functions implements IForgeRegistry.AddCallback<Function>, IForgeRegistry.CreateCallback<Function> {

        @Override
        public void onAdd(IForgeRegistryInternal<Function> owner, RegistryManager stage, int id, ResourceKey<Function> key, Function obj, @Nullable Function oldObj) {
            if (oldObj instanceof SpecialFunction oldSpecial && obj instanceof SpecialFunction newSpecial) {
                List<SpecialFunction> specials = owner.getSlaveMap(SPECIAL_MAP, List.class);
                specials.remove(oldSpecial);
                specials.add(newSpecial);
            }
        }

        @Override
        public void onCreate(IForgeRegistryInternal<Function> owner, RegistryManager stage) {
            owner.setSlaveMap(SPECIAL_MAP, Lists.newArrayList());
        }
    }
}
