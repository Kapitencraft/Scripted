package net.kapitencraft.scripted.io;

import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface IOHelper {
    File SCRIPTED_DIRECTORY = new File("./scripted"); //bruh

    static List<File> listResources(File file) {
        if (!file.exists()) return List.of();
        if (!file.isDirectory()) return List.of(file);
        List<File> finals = new ArrayList<>();
        List<File> queue = new ArrayList<>();
        queue.add(file);
        while (!queue.isEmpty()) {
            if (queue.get(0).isDirectory()) {
                String[] childNames = queue.get(0).list();
                if (childNames != null) for (String childName : childNames) {
                    queue.add(new File(queue.get(0), childName));
                }
            } else {
                finals.add(queue.get(0));
            }
            queue.remove(0);
        }
        return finals;
    }

    static List<ResourceLocation> toNames(List<File> files) {
        File file = new File("test");
        return files.stream().map(File::getPath).map(s -> {
            String[] directories = s.split("\\\\"); //why 4 bro?
            return new ResourceLocation("a");
        }).toList();
    }

    static @Nullable VarType<?> readFromCode(String codeReference) {
        for (Map.Entry<ResourceKey<VarType<?>>, VarType<?>> entry : ModRegistries.VAR_TYPES.getEntries()) {
            if (Objects.equals(entry.getKey().location().getPath(), codeReference)) {
                return entry.getValue();
            }
        }
        return null;
    }

    static @Nullable Function readFunction(String codeReference) {
        for (Map.Entry<ResourceKey<Function>, Function> entry : ModRegistries.FUNCTIONS.getEntries()) {
            if (Objects.equals(entry.getKey().location().getPath(), codeReference)) {
                return entry.getValue();
            }
        }
        return null;
    }
}