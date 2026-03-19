package net.kapitencraft.scripted.lang.exe.natives;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.List;

/**
 * registration handler for classes not added by your own mod (use {@link NativeClass} otherwise)
 */
public class ClassRegistration {
    private final List<NativeClassLoader.ClassObj> classes;

    ClassRegistration(List<NativeClassLoader.ClassObj> classes) {
        this.classes = classes;
    }

    public void registerClassWithRegistry(Class<?> clazz, ResourceKey<? extends Registry<?>> key) {
        registerClassWithRegistry(clazz, key, clazz.getPackageName());
    }

    private void registerClassWithRegistry(Class<?> clazz, ResourceKey<? extends Registry<?>> key, String packageName) {
        registerClassWithRegistry(clazz, key, packageName, clazz.getSimpleName());
    }

    private void registerClassWithRegistry(Class<?> clazz, ResourceKey<? extends Registry<?>> key, String pck, String name) {
        classes.add(new NativeClassLoader.RegisteredClassObj(clazz, key, null, null, name, pck));
    }

    public void registerClass(Class<?> clazz) {
        registerClass(clazz, clazz.getPackageName());
    }

    public void registerClass(Class<?> clazz, String pck) {
        registerClass(clazz, pck, clazz.getSimpleName());
    }

    public void registerClass(Class<?> clazz, String pck, String name) {
        classes.add(new NativeClassLoader.RegisteredClassObj(clazz, null, null, null, name, pck));
    }

    public void registerClass(Class<?> clazz, String pck, String name, String[] capturedMethods, String... capturedFields) {
        classes.add(new NativeClassLoader.RegisteredClassObj(clazz, null, capturedMethods, capturedFields.length == 0 ? null : capturedFields, name, pck));
    }

    public void registerClass(Class<?> clazz, String pck, String name, String... capturedMethods) {
        classes.add(new NativeClassLoader.RegisteredClassObj(clazz, null, capturedMethods.length == 0 ? null : capturedMethods, null, name, pck));
    }

}
