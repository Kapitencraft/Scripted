package net.kapitencraft.scripted.lang.oop.clazz.skeleton;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.Holder;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.load.ClassLoader;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.field.SkeletonField;
import net.kapitencraft.scripted.lang.oop.method.SkeletonMethod;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.oop.method.map.GeneratedMethodMap;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

public class SkeletonInterface implements ScriptedClass {
    private final String name;
    private final String pck;

    private final String[] interfaces;

    private final Map<String, SkeletonField> staticFields;

    private final Holder.Generics generics;

    private final GeneratedMethodMap methods;

    public SkeletonInterface(String name, String pck, String[] interfaces, Map<String, SkeletonField> staticFields, Holder.Generics generics, Map<String, DataMethodContainer> methods) {
        this.name = name;
        this.pck = pck;
        this.interfaces = interfaces;
        this.staticFields = staticFields;
        this.generics = generics;
        this.methods = new GeneratedMethodMap(methods);
    }

    public static ScriptedClass fromCache(JsonObject data, String pck) {
        String name = GsonHelper.getAsString(data, "name");

        ImmutableMap<String, DataMethodContainer> methods = SkeletonMethod.readFromCache(data, "methods");

        ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(data, "staticFields");
            fieldData.asMap().forEach((s, element) -> {
                JsonObject object = element.getAsJsonObject();
                staticFields.put(s, new SkeletonField(ClassLoader.loadClassReference(object, "type"), GsonHelper.getAsShort(object, "modifiers")));
            });
        }

        String[] interfaces = ClassLoader.loadInterfaces(data);

        return new SkeletonInterface(
                name,
                pck,
                interfaces,
                staticFields.build(),
                null,
                methods
        );
    }

    @Override
    public Object getStaticField(String name) {
        return null;
    }

    @Override
    public Object setStaticField(String name, Object val) {
        return null;
    }

    @Override
    public @Nullable Holder.Generics getGenerics() {
        return generics;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String pck() {
        return pck;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return null;
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return methods.getMethod(signature);
    }

    @Override
    public short getModifiers() {
        return Modifiers.INTERFACE;
    }

    @Override
    public GeneratedMethodMap getMethods() {
        return methods;
    }

    @Override
    public ClassReference[] interfaces() {
        return Arrays.stream(interfaces).map(VarTypeManager::directParseType).toArray(ClassReference[]::new);
    }

    @Override
    public Annotation[] annotations() {
        return new Annotation[0];
    }

    @Override
    public boolean hasMethod(String name) {
        return methods.has(name);
    }

    @Override
    public boolean isNative() {
        return false;
    }
}
