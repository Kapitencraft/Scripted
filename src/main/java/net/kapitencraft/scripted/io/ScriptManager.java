package net.kapitencraft.scripted.io;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import net.kapitencraft.kap_lib.helpers.IOHelper;
import net.kapitencraft.kap_lib.io.JsonHelper;
import net.kapitencraft.scripted.Markers;
import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.script.Script;
import net.kapitencraft.scripted.code.script.type.ScriptType;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptManager extends SimpleJsonResourceReloadListener {
    private final Multimap<ScriptType<?, ?>, Script<?>> scripts = HashMultimap.create();

    public ScriptManager() {
        super(JsonHelper.GSON, "scripts");
    }

    @Override
    protected @NotNull Map<ResourceLocation, JsonElement> prepare(@NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        Map<ResourceLocation, JsonElement> map = new HashMap<>();
        ModRegistries.SCRIPT_TYPES.forEach(scriptType -> {
            File directory = scriptType.getDataDirectory();
            List<File> list = IOHelper.listResources(directory);
            list.forEach(file -> {
                String path = file.getPath();
                String[] directories = path.split("\\\\");
                ResourceLocation location = ModRegistries.SCRIPT_TYPES.getKey(scriptType);
                assert location != null;
                String name = directories[directories.length - 1];
                location = location.withPath(location.getPath() + "/" + name.replace("." + scriptType.getFileSuffix(), ""));
                try {
                    map.put(location, JsonHelper.GSON.fromJson(new FileReader(file), JsonElement.class));
                } catch (FileNotFoundException e) {
                    Scripted.LOGGER.warn(Markers.SCRIPT_MANAGER, "error finding script '{}': {}", location, e.getMessage());
                }
            });
        });
        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        data.forEach((resourceLocation, jsonElement) -> {
            try {
                ResourceLocation elementId = resourceLocation.withPath(resourceLocation.getPath().split("/")[0]);
                ScriptType<?, ?> type = ModRegistries.SCRIPT_TYPES.getValue(elementId);
                assert type != null;
                this.scripts.put(type, type.load(jsonElement));
            } catch (Exception e) {
                Scripted.LOGGER.warn(Markers.SCRIPT_MANAGER, "error loading script '{}': {}", resourceLocation, e.getMessage());
            }
        });
    }
}
