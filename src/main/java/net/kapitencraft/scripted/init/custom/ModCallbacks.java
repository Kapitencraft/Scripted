package net.kapitencraft.scripted.init.custom;

import com.google.common.collect.Maps;
import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.StartupMessageManager;
import net.minecraftforge.fml.loading.progress.ProgressMeter;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ModCallbacks {
    public static class VarTypes implements IForgeRegistry.AddCallback<VarType<?>>, IForgeRegistry.CreateCallback<VarType<?>>, IForgeRegistry.BakeCallback<VarType<?>> {
        public static final ResourceLocation NAME_MAP = Scripted.res("name_map");
        public static final ResourceLocation PRIMITIVES = Scripted.res("primitives");
        public static final ResourceLocation REGISTRIES = Scripted.res("registries");

        @Override
        public void onAdd(IForgeRegistryInternal<VarType<?>> owner, RegistryManager stage, int id, ResourceKey<VarType<?>> key, VarType<?> obj, @Nullable VarType<?> oldObj) {
            if (oldObj == null) {
                obj.bakeMethods();
            }

            //names
            Map<String, VarType<?>> nameMap = owner.getSlaveMap(NAME_MAP, Map.class);
            if (oldObj != null) removeOldName(oldObj, nameMap);
            nameMap.put(obj.getName(), obj);

            //primitives
            List<PrimitiveType<?>> primitives = owner.getSlaveMap(PRIMITIVES, List.class);
            if (oldObj instanceof PrimitiveType<?>) primitives.remove(oldObj);
            if (obj instanceof PrimitiveType<?> prim) {
                primitives.add(prim);
            }

            //registries
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

        @Override
        public void onBake(IForgeRegistryInternal<VarType<?>> owner, RegistryManager stage) {
            Collection<VarType<?>> collection = owner.getValues();
            ProgressMeter meter = StartupMessageManager.addProgressBar("Firing Extra Method Events...", collection.size());
            collection.forEach(varType -> {
                varType.fireExtraMethodsEvent();
                meter.increment();
            });
            meter.complete();
            collection.forEach(VarType::createMethods);
            collection.forEach(VarType::bakeMethods);
        }
    }
}