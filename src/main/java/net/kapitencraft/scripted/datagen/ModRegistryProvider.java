package net.kapitencraft.scripted.datagen;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.edit.graphical.selection.SelectionTabs;
import net.kapitencraft.scripted.registry.ModRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModRegistryProvider extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(ModRegistries.Keys.SELECTION_TABS, SelectionTabs::bootstrap);

    public ModRegistryProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Scripted.MOD_ID));
    }
}
