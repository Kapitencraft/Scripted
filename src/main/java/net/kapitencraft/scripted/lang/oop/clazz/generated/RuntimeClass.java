package net.kapitencraft.scripted.lang.oop.clazz.generated;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.MethodLookup;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.load.ClassLoader;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.field.RuntimeField;
import net.kapitencraft.scripted.lang.oop.field.ScriptedField;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.scripted.lang.tool.Util;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class RuntimeClass implements ScriptedClass {
    private final Map<String, Object> staticFields = new HashMap<>();

    private final GeneratedMethodMap methods;

    private final MethodLookup lookup;

    private final Map<String, RuntimeField> allFields;

    private final String superclass;
    private final String[] implemented;
    private final String name;
    private final String packageRepresentation;
    private final Annotation[] annotations;

    private final short modifiers;

    public RuntimeClass(Map<String, DataMethodContainer> methods,
                        Map<String, RuntimeField> fields,
                        String superclass, String name, String packageRepresentation,
                        String[] implemented,
                        short modifiers, Annotation[] annotations) {
        this.methods = new GeneratedMethodMap(methods);
        this.allFields = fields;
        this.superclass = superclass;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.implemented = implemented;
        this.modifiers = modifiers;
        this.lookup = MethodLookup.createFromClass(this);
        this.annotations = annotations;
    }

    public static ScriptedClass load(JsonObject data, String pck) {
        String name = GsonHelper.getAsString(data, "name");
        String superclass = GsonHelper.getAsString(data, "superclass");
        String[] implemented = ClassLoader.loadInterfaces(data);

        ImmutableMap<String, DataMethodContainer> methods = DataMethodContainer.load(data, name, "methods");

        ImmutableMap<String, RuntimeField> fields = RuntimeField.loadFieldMap(data, "fields");

        short modifiers = data.has("modifiers") ? GsonHelper.getAsShort(data, "modifiers") : 0;

        Annotation[] annotations = Annotation.readAnnotations(data);

        return new RuntimeClass(
                methods,
                fields,
                superclass,
                name, pck,
                implemented,
                modifiers,
                annotations
        );
    }

    @Override
    public ClassReference getFieldType(String name) {
        return Optional.ofNullable(getFields().get(name)).map(ScriptedField::type).orElse(ScriptedClass.super.getFieldType(name));
    }

    @Override
    public boolean hasField(String name) {
        return allFields.containsKey(name) || ScriptedClass.super.hasField(name);
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return lookup.get(signature);
    }

    @Override
    public boolean hasMethod(String name) {
        return methods.has(name) || ScriptedClass.super.hasMethod(name);
    }

    @Override
    public Map<String, ? extends ScriptedField> getFields() {
        return Util.mergeMaps(ScriptedClass.super.getFields(), allFields);
    }

    @Override
    public boolean isAbstract() {
        return Modifiers.isAbstract(modifiers);
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(modifiers);
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public GeneratedMethodMap getMethods() {
        return methods;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return VarTypeManager.directParseType(superclass);
    }

    @Override
    public Object getStaticField(String name) {
        return staticFields.get(name);
    }

    @Override
    public Object setStaticField(String name, Object val) {
        return staticFields.put(name, val);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String pck() {
        return packageRepresentation;
    }

    @Override
    public String toString() { //jesus
        return "GeneratedClass{" + name + "}[" +
                "methods=" + methods.asMap() + ", " +
                "fields=" + allFields + ", " +
                "superclass=" + superclass + ']';
    }

    @Override
    public ClassReference[] interfaces() {
        return Arrays.stream(implemented).map(VarTypeManager::directParseType).toArray(ClassReference[]::new);
    }

    @Override
    public Annotation[] annotations() {
        return annotations;
    }

    @Override
    public short getModifiers() {
        return modifiers;
    }

    @Override
    public boolean isNative() {
        return false;
    }
}