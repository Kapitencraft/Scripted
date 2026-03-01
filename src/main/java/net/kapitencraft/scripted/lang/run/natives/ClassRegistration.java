package net.kapitencraft.scripted.lang.run.natives;

import java.util.List;

/**
 * registration handler for classes not added by your own mod (use {@link NativeClass} otherwise)
 */
public class ClassRegistration {
    private final List<NativeClassLoader.ClassObj> classes;

    ClassRegistration(List<NativeClassLoader.ClassObj> classes) {
        this.classes = classes;
    }

    public void registerClass(Class<?> clazz) {
        registerClass(clazz, clazz.getPackageName());
    }

    public void registerClass(Class<?> clazz, String pck) {
        registerClass(clazz, pck, clazz.getSimpleName());
    }

    public void registerClass(Class<?> clazz, String pck, String name) {
        classes.add(new NativeClassLoader.RegisteredClassObj(clazz, null, null, name, pck));
    }

    public void registerClass(Class<?> clazz, String pck, String name, String[] capturedMethods, String... capturedFields) {
        classes.add(new NativeClassLoader.RegisteredClassObj(clazz, capturedMethods, capturedFields.length == 0 ? null : capturedFields, name, pck));
    }

    public void registerClass(Class<?> clazz, String pck, String name, String... capturedMethods) {
        classes.add(new NativeClassLoader.RegisteredClassObj(clazz, capturedMethods.length == 0 ? null : capturedMethods, null, name, pck));
    }

}
