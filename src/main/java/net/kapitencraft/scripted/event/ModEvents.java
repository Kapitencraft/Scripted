package net.kapitencraft.scripted.event;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.edit.graphical.selection.SelectionTab;
import net.kapitencraft.scripted.lang.exe.load.ClassLoader;
import net.kapitencraft.scripted.registry.ModRegistries;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.io.File;
import java.io.IOException;

@EventBusSubscriber
public class ModEvents {

    @SubscribeEvent
    public static void onDataPackRegistryNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(ModRegistries.Keys.SELECTION_TABS, SelectionTab.CODEC, SelectionTab.CODEC);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        File file = new File(event.getServer().getWorldPath(LevelResource.ROOT).toFile(), "scripted/cache");

        try {
            ClassLoader.load(file);
        } catch (IOException e) {
            Scripted.LOGGER.warn("Unable to load cached source: {}", e.getMessage());
        }
    }
}
