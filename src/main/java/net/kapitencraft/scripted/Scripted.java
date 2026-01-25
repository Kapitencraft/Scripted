package net.kapitencraft.scripted;

import com.mojang.logging.LogUtils;
import net.kapitencraft.scripted.edit.OpenEditScreenCommand;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.io.File;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Scripted.MOD_ID)
public class Scripted {
    public static File SCRIPTED_DIRECTORY = new File("./scripted"); //bruh

    public static final String MOD_ID = "scripted";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static <T> DeferredRegister<T> createRegistry(ResourceKey<Registry<T>> key) {
        return DeferredRegister.create(key, MOD_ID);
    }

    public Scripted(IEventBus bus) {
    }

    public static ResourceLocation res(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }

    @EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent
        public static void addReloadListeners(AddReloadListenerEvent event) {
            //event.addListener(new ScriptManager());
        }

        @SubscribeEvent
        public static void addCommands(RegisterClientCommandsEvent event) {
            OpenEditScreenCommand.register(event.getDispatcher());
        }
    }
}
