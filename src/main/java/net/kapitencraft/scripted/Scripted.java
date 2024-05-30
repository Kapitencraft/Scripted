package net.kapitencraft.scripted;

import com.mojang.logging.LogUtils;
import net.kapitencraft.scripted.init.custom.ModRegistryBuilders;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
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

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

    }

    public static ResourceLocation res(String id) {
        return new ResourceLocation(MOD_ID, id);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MOD_ID)
    public static class BusEventsHandler {
        @SubscribeEvent
        public static void addRegistries(NewRegistryEvent event) {
            event.create(ModRegistryBuilders.METHODS_BUILDER);
            event.create(ModRegistryBuilders.VAR_TYPE_BUILDER);
        }
    }
}
