package net.kapitencraft.scripted;

import com.mojang.logging.LogUtils;
import net.kapitencraft.scripted.init.ModFunctions;
import net.kapitencraft.scripted.init.ModMethods;
import net.kapitencraft.scripted.init.ModScriptTypes;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.kapitencraft.scripted.init.custom.ModRegistryBuilders;
import net.kapitencraft.scripted.io.ScriptManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Scripted.MOD_ID)
public class Scripted {

    public static final String MOD_ID = "scripted";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static <T> DeferredRegister<T> createRegistry(ResourceKey<Registry<T>> key) {
        return DeferredRegister.create(key, MOD_ID);
    }

    public Scripted()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModFunctions.REGISTRY.register(modEventBus);
        ModVarTypes.REGISTRY.register(modEventBus);
        ModScriptTypes.REGISTRY.register(modEventBus);
        ModMethods.REGISTRY.register(modEventBus);
    }

    public static ResourceLocation res(String id) {
        return new ResourceLocation(MOD_ID, id);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MOD_ID)
    public static class BusEventsHandler {
        @SubscribeEvent
        public static void addRegistries(NewRegistryEvent event) {
            event.create(ModRegistryBuilders.VAR_TYPE_BUILDER);
            event.create(ModRegistryBuilders.FUNCTION_BUILDER);
            event.create(ModRegistryBuilders.SCRIPT_TYPES);
            event.create(ModRegistryBuilders.METHODS);
        }
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent
        public static void addReloadListeners(AddReloadListenerEvent event) {
            event.addListener(new ScriptManager());
        }
    }
}
