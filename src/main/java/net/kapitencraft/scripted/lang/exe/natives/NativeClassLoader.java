package net.kapitencraft.scripted.lang.exe.natives;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.natives.impl.NativeClassImpl;
import net.kapitencraft.scripted.lang.exe.natives.impl.NativeConstructor;
import net.kapitencraft.scripted.lang.exe.natives.impl.NativeMethod;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.field.NativeField;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.scripted.lang.tool.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.progress.ProgressMeter;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.reflect.*;
import java.util.*;

public class NativeClassLoader {
    public static final Logger LOGGER = LoggerFactory.getLogger("NativeClassLoader");
    @ApiStatus.Internal
    private static final Map<Class<?>, ClassReference> classLookup = new HashMap<>();

    static {
        registerNative(VarTypeManager.NUMBER, Number.class);
        registerNative(VarTypeManager.INTEGER, Integer.class, int.class);
        registerNative(VarTypeManager.FLOAT, Float.class, float.class);
        registerNative(VarTypeManager.DOUBLE, Double.class, double.class);
        registerNative(VarTypeManager.BOOLEAN, Boolean.class, boolean.class);
        registerNative(VarTypeManager.CHAR, Character.class, char.class);
        registerNative(VarTypeManager.VOID, Void.class, void.class);
        classLookup.put(Enum.class, VarTypeManager.ENUM);
    }

    private static boolean hadError = false;

    private static void registerNative(PrimitiveClass target, Class<?>... types) {
        for (Class<?> type : types) {
            classLookup.put(type, target.reference());
        }
    }

    @ApiStatus.Internal
    public static void load() {
        LOGGER.info("loading natives...");

        Collection<Class<?>> plugins = getTypesAnnotatedWith(ScriptedPlugin.class);
        StartupNotificationManager.addModMessage("Found " + plugins.size() + " plugin(s)");

        List<ClassObj> nativeClasses = new ArrayList<>();

        ClassRegistration registration = new ClassRegistration(nativeClasses);
        ProgressMeter meter = StartupNotificationManager.addProgressBar("Loading native classes...", 0);
        plugins.forEach(c -> handlePlugin(c, registration));
        meter.complete();

        registerAnnotated(nativeClasses, getTypesAnnotatedWith(NativeClass.class));

        nativeClasses.forEach(ClassObj::registerReference);
        nativeClasses.forEach(ClassObj::loadClass);

        if (hadError) {
            LOGGER.error("NativeLoader detected broken load state. shutting down!");
            System.exit(65);
        }
        LOGGER.info("Total registered classes: {}", nativeClasses.size());
        StartupNotificationManager.addModMessage("Total registered classes: " + nativeClasses.size());
    }

    private static Collection<Class<?>> getTypesAnnotatedWith(Class<? extends java.lang.annotation.Annotation> clazz) {
        List<ModFileScanData.AnnotationData> list = ModList.get().getAllScanData().stream().flatMap(d -> d.getAnnotatedBy(clazz, ElementType.TYPE)).toList();

        Type type = Type.getType(clazz);
        List<Class<?>> obj = new ArrayList<>();
        for (ModFileScanData.AnnotationData data : list) {
            if (data.annotationType().equals(type)) {
                try {
                    obj.add(Class.forName(data.clazz().getClassName()));
                } catch (ClassNotFoundException e) {
                    LOGGER.warn("type not found: {}", data.clazz().getClassName());
                }
            }
        }
        return obj;
    }

    private static void registerAnnotated(List<ClassObj> nativeClasses, Collection<Class<?>> annotated) {
        annotated.stream().map(AnnotatedClassObj::new).forEach(nativeClasses::add);
    }

    private static void handlePlugin(Class<?> c, ClassRegistration registration) {
        try {
            Method method = c.getMethod("registerClasses", ClassRegistration.class);
            method.invoke(null, registration);
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException | IllegalAccessException e) {
            LOGGER.warn("unable to execute plugin method 'registerClasses': {}", e.getMessage());
        }

    }

    private static synchronized void setupLookup(Class<?> clazz, String name, String pck) {
        ClassReference reference = VarTypeManager.getOrCreateClass(name, pck);
        reference.setTarget(new NativeWrapper(name, pck)); //set target for reference
        classLookup.put(clazz, reference);
    }

    /**
     * creates and registers a native class for the given java class
     * <br>Developers should use {@link ScriptedPlugin} or {@link NativeClass}
     *
     * @param clazz         the target to create & register
     */
    private static void createNativeClass(Class<?> clazz, String className, String pck, @Nullable String[] capturedMethods, @Nullable String[] capturedFields, ResourceKey<? extends Registry<?>> owner) {
        try {
            Multimap<String, ScriptedCallable> methods = HashMultimap.create();
            for (Method declaredMethod : clazz.getDeclaredMethods()) {
                int modifiers = declaredMethod.getModifiers();
                if (Modifier.isPublic(modifiers) && !declaredMethod.isAnnotationPresent(Excluded.class) && (capturedMethods == null || Util.arrayContains(capturedMethods, declaredMethod.getName()))) {
                    try {
                        boolean isStatic = Modifier.isStatic(modifiers);
                        String methodName = declaredMethod.isAnnotationPresent(Rename.class) ? declaredMethod.getAnnotation(Rename.class).value() : declaredMethod.getName();
                        NativeMethod method = new NativeMethod(
                                getClassOrThrow(declaredMethod.getReturnType()),
                                Arrays.stream(declaredMethod.getParameterTypes()).map(NativeClassLoader::getClassOrThrow).toArray(ClassReference[]::new),
                                declaredMethod,
                                !isStatic,
                                Modifiers.fromJavaMods(modifiers)
                        );
                        methods.put(methodName, method);
                    } catch (RuntimeException ignored) {
                    }
                }
            }

            Map<String, NativeField> fields = new HashMap<>();
            for (Field declaredField : clazz.getDeclaredFields()) {
                try {
                    String fieldName = declaredField.isAnnotationPresent(Rename.class) ? declaredField.getAnnotation(Rename.class).value() : declaredField.getName();
                    if (Modifier.isPublic(declaredField.getModifiers()) && !declaredField.isAnnotationPresent(Excluded.class) && (capturedFields == null || Util.arrayContains(capturedFields, fieldName))) {
                        NativeField impl = new NativeField(
                                getClassOrThrow(declaredField.getType()),
                                Modifiers.fromJavaMods(declaredField.getModifiers()),
                                declaredField
                        );
                        fields.put(fieldName, impl);
                    }
                } catch (RuntimeException ignored) {
                }
            }
            ClassReference type = getClassOrThrow(clazz);
            for (Constructor<?> constructor : clazz.getConstructors()) {
                try {
                    if (!constructor.isAnnotationPresent(Excluded.class)) {
                        NativeConstructor method = new NativeConstructor(
                                type,
                                Arrays.stream(constructor.getParameterTypes()).map(NativeClassLoader::getClassOrThrow).toArray(ClassReference[]::new),
                                constructor
                        );
                        methods.put("<init>", method);
                    }
                } catch (RuntimeException ignored) {
                }
            }

            NativeClassImpl target = new NativeClassImpl(className, pck,
                    bakeMethods(methods), fields,
                    getSuperClass(clazz.getSuperclass()),
                    extractInterfaces(clazz.getInterfaces()),
                    owner,
                    Modifiers.fromJavaMods(clazz.getModifiers())
            );
            type.setTarget(target);
            VarTypeManager.registerFlat(target);
        } catch (Exception e) {
            LOGGER.warn("Failed to load class '{}': {}", clazz.getName(), e.getMessage());
            hadError = true;
        }
    }

    private static ClassReference getSuperClass(Class<?> superclass) {
        if (superclass == null) //class is object; no superclass present
            return null;

        return getClass(superclass).orElse(VarTypeManager.OBJECT);
    }

    private static ClassReference[] extractInterfaces(Class<?>[] interfaces) {
        List<ClassReference> extensions = new ArrayList<>();
        for (Class<?> c : interfaces) {
            getClass(c).ifPresent(extensions::add);
        }
        return extensions.toArray(ClassReference[]::new);
    }

    private static Optional<ClassReference> getClassOrThrowIfAllowed(Class<?> type, boolean ignoreMissing) {
        return type == null ? null : ignoreMissing ? getClass(type) : Optional.ofNullable(getClassOrThrow(type));
    }

    private static ClassReference getClassOrThrow(Class<?> aClass) {
        if (aClass == null) return null;
        int arrayCount = 0;
        while (aClass.isArray()) {
            arrayCount++;
            aClass = aClass.getComponentType();
        }
        if (!classLookup.containsKey(aClass))
            throw new RuntimeException("class '" + aClass.getName() + "' not registered");
        ClassReference c = classLookup.get(aClass);
        while (arrayCount > 0) {
            c = c.array();
            arrayCount--;
        }
        return c;
    }

    /**
     * queries the given class for lookup
     *
     * @param aClass the class to query
     * @return an optional of the gotten reference
     */
    public static Optional<ClassReference> getClass(Class<?> aClass) {
        if (aClass == null) return Optional.empty();
        int arrayCount = 0;
        while (aClass.isArray()) {
            arrayCount++;
            aClass = aClass.getComponentType();
        }

        ClassReference c = classLookup.get(aClass);
        if (c == null) return Optional.empty();
        while (arrayCount > 0) {
            c = c.array();
            arrayCount--;
        }
        return Optional.of(c);
    }

    /**
     * extracts a native value out of a {@link NativeClassInstance} wrapper, or throws if it can't
     */
    public static Object extractNative(Object reference) {
        if (reference instanceof Number ||
                reference instanceof String ||
                reference == null ||
                reference instanceof Boolean ||
                reference instanceof Character ||
                reference instanceof char[] ||
                reference instanceof int[] ||
                reference instanceof double[] ||
                reference instanceof float[] ||
                reference instanceof boolean[]
        ) return reference;
        if (reference instanceof Object[] objects) {
            return Arrays.stream(objects).map(NativeClassLoader::extractNative).toArray(Object[]::new);
        }
        if (reference instanceof NativeClassInstance NCI) {
            return NCI.getObject();
        }
        throw new IllegalArgumentException("cannot extract native from '" + reference + "'");
    }

    private static Map<String, DataMethodContainer> bakeMethods(Multimap<String, ScriptedCallable> methods) {
        ImmutableMap.Builder<String, DataMethodContainer> builder = new ImmutableMap.Builder<>();
        for (String s : methods.keySet()) {
            Collection<ScriptedCallable> nativeMethods = methods.get(s);
            DataMethodContainer container = new DataMethodContainer(nativeMethods.toArray(new ScriptedCallable[0]));
            builder.put(s, container);
        }
        return builder.build();
    }

    public static Object[] extractNatives(Object[] in, boolean exclude0) {
        return (exclude0 ? Arrays.stream(in, 1, in.length) : Arrays.stream(in)).map(NativeClassLoader::extractNative).toArray();
    }

    public static Object wrapString(@NotNull String s) {
        return new NativeClassInstance((NativeClassImpl) VarTypeManager.STRING.get(), s);
    }

    interface ClassObj {
        void loadClass();

        void registerReference();
    }

    static class AnnotatedClassObj implements ClassObj {
        private final Class<?> target;

        AnnotatedClassObj(Class<?> target) {
            this.target = target;
        }

        @Override
        public void loadClass() {
            if (target.isAnnotationPresent(NativeClass.class)) {
                NativeClass nativeClass = target.getAnnotation(NativeClass.class);
                String pck = nativeClass.pck();
                String className = nativeClass.name();
                if (className.isEmpty()) className = target.getSimpleName();
                NativeClassLoader.createNativeClass(target, className, pck, null, null, null);
            } else {
                LOGGER.warn("can not create native class for '{}': missing NativeClass annotation", target.getCanonicalName());
            }
        }

        @Override
        public void registerReference() {
            if (target.isAnnotationPresent(NativeClass.class)) {
                NativeClass nativeClass = target.getAnnotation(NativeClass.class);
                String pck = nativeClass.pck();
                String className = nativeClass.name();
                if (className.isEmpty()) className = target.getSimpleName();
                NativeClassLoader.setupLookup(target, className, pck);
            }
        }
    }

    static class RegisteredClassObj implements ClassObj {
        /**
         * target class
         */
        private final Class<?> target;
        /**
         * specify methods and fields to capture
         */
        private final ResourceKey<? extends Registry<?>> owner;
        private final String[] capturedMethods, capturedFields;
        private final String name, pck;

        RegisteredClassObj(Class<?> target, ResourceKey<? extends Registry<?>> owner, String[] capturedMethods, String[] capturedFields, String name, String pck) {
            this.target = target;
            this.owner = owner;
            this.capturedMethods = capturedMethods;
            this.capturedFields = capturedFields;
            this.name = name;
            this.pck = pck;
        }

        @Override
        public void loadClass() {
            try {
                NativeClassLoader.createNativeClass(target, name, pck, capturedMethods, capturedFields, owner);
            } catch (Exception e) {
                System.err.println("Failed to load class '" + target.getName() + "': " + e.getMessage());
            }
        }

        @Override
        public void registerReference() {
            NativeClassLoader.setupLookup(target, name, pck);
        }
    }

    private record NativeWrapper(String name, String pck) implements ScriptedClass {

        @Override
        public @Nullable ClassReference superclass() {
            return null;
        }

        @Override
        public ScriptedCallable getMethod(String signature) {
            return null;
        }

        @Override
        public AbstractMethodMap getMethods() {
            return null;
        }

        @Override
        public Annotation[] annotations() {
            return new Annotation[0];
        }

        @Override
        public short getModifiers() {
            return 0;
        }

        @Override
        public boolean isNative() {
            return true;
        }

        @Override
        public Object getStaticField(String name) {
            throw new IllegalAccessError("can not get static field from NativeWrapper");
        }

        @Override
        public Object setStaticField(String name, Object val) {
            throw new IllegalAccessError("can not set static field from NativeWrapper");
        }
    }
}
