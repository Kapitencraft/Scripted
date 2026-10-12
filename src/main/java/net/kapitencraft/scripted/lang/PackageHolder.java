package net.kapitencraft.scripted.lang;

import net.kapitencraft.scripted.lang.exe.load.ClassLoaderHolder;
import net.kapitencraft.scripted.lang.oop.Package;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PackageHolder<T extends ClassLoaderHolder<T>> {
    private final Map<String, PackageHolder<T>> packages = new HashMap<>();
    private final Map<String, T> classes = new HashMap<>();

    public void add(String pck, String name, T val) {
        String[] packages = pck.split("\\.");
        PackageHolder<T> holder = this.packages.get(packages[0]);
        for (int i = 1; i < packages.length; i++) {
            holder = holder.getOrCreate(packages[i]);
        }
        holder.classes.put(name, val);
    }

    public PackageHolder<T> getOrCreate(String name) {
        return packages.computeIfAbsent(name, n -> new PackageHolder<>());
    }

    public void forEach(Consumer<T> sink) {
        classes.values().forEach(sink);
        packages.values().forEach(h -> h.forEach(sink));
    }

    public int size() {
        int size = this.classes.size();
        for (PackageHolder<T> value : this.packages.values()) {
            size += value.size();
        }
        return size;
    }

    public boolean isEmpty() {
        return this.packages.isEmpty() && this.classes.isEmpty();
    }

    public void useClasses(BiConsumer<Map<String, T>, Package> consumer, Package pck) {
        consumer.accept(this.classes, pck);
        this.packages.forEach((name, holder1) ->
                holder1.useClasses(consumer, pck.getOrCreatePackage(name))
        );
    }

    public static <T extends ClassLoaderHolder<T>> PackageHolder<T> load(File fileLoc, String end, Function<File, T> constructor) {
        PackageHolder<T> root = new PackageHolder<>();
        loadRecursive(fileLoc, root, end, constructor);
        return root;
    }

    private static <T extends ClassLoaderHolder<T>> void loadRecursive(File root, PackageHolder<T> holder, String extension, Function<File, T> constructor) {
        File[] files = root.listFiles();
        if (files == null) {
            return;
        }
        for (File file1 : files) {
            if (file1.isDirectory()) {
                PackageHolder<T> child = new PackageHolder<>();
                holder.packages.put(file1.getName(), child);
                loadRecursive(file1, child, extension, constructor);
            } else {
                String name = file1.getName().replace(extension, "");
                holder.classes.put(name, constructor.apply(file1));
            }
        }
    }
}
