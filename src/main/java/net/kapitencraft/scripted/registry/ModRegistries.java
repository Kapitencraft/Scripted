package net.kapitencraft.scripted.registry;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.edit.graphical.selection.SelectionTab;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface ModRegistries {

    interface Keys {
        ResourceKey<Registry<SelectionTab>> SELECTION_TABS = reg("selection_tabs");

        private static ResourceKey<Registry<SelectionTab>> reg(String name) {
            return ResourceKey.createRegistryKey(Scripted.res(name));
        }
    }
}
