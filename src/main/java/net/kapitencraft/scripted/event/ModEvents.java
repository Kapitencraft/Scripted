package net.kapitencraft.scripted.event;

import net.kapitencraft.scripted.edit.graphical.selection.SelectionTab;
import net.kapitencraft.scripted.registry.ModRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber
public class ModEvents {

    @SubscribeEvent
    public static void onDataPackRegistryNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(ModRegistries.Keys.SELECTION_TABS, SelectionTab.CODEC, SelectionTab.CODEC);
    }
}
