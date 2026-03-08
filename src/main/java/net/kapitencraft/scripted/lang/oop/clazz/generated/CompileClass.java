package net.kapitencraft.scripted.lang.oop.clazz.generated;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.kap_lib.core.collection.MapStream;
import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.CacheBuilder;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.CacheableClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.field.CompileField;
import net.kapitencraft.scripted.lang.oop.field.RuntimeField;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.oop.method.RuntimeCallable;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.scripted.lang.oop.method.map.GeneratedMethodMap;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

public final class CompileClass implements CacheableClass, ScriptedClass {
    private final GeneratedMethodMap methods;

    private final Map<String, CompileField> allFields;

    private final ClassReference superclass;
    private final ClassReference[] implemented;
    private final String name;
    private final String packageRepresentation;

    private final short modifiers;

    private final Annotation[] annotations;

    public CompileClass(Map<String, DataMethodContainer> methods,
                        Map<String, CompileField> fields,
                        ClassReference superclass, String name, String packageRepresentation,
                        ClassReference[] implemented,
                        short modifiers, Annotation[] annotations) {
        this.methods = new GeneratedMethodMap(methods);
        this.allFields = fields;
        this.superclass = superclass;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.implemented = implemented;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "class");
        object.addProperty("name", name);
        object.addProperty("superclass", VarTypeManager.getClassName(superclass));
        {
            JsonArray parentInterfaces = new JsonArray();
            Arrays.stream(this.implemented).map(VarTypeManager::getClassName).forEach(parentInterfaces::add);
            object.add("interfaces", parentInterfaces);
        }
        object.add("methods", methods.save(cacheBuilder));
        {
            JsonObject fields = new JsonObject();
            allFields.forEach((name, field) -> fields.add(name, field.cache(cacheBuilder)));
            object.add("fields", fields);
        }

        object.add("annotations", cacheBuilder.cacheAnnotations(this.annotations));

        if (this.modifiers != 0) object.addProperty("modifiers", modifiers);

        return object;
    }

    public RuntimeClass convert() {
        return new RuntimeClass(
                convertMethods(methods),
                convertFields(allFields),
                VarTypeManager.getClassName(superclass),
                this.name,
                this.pck(),
                Arrays.stream(this.implemented).map(VarTypeManager::getClassName).toArray(String[]::new),
                this.modifiers,
                this.annotations
        );
    }

    private Map<String, DataMethodContainer> convertMethods(GeneratedMethodMap methods) {
        return MapStream.of(methods.asMap()).mapValues(this::convertMethodContainer).toMap();
    }

    private DataMethodContainer convertMethodContainer(DataMethodContainer d) {
        return DataMethodContainer.of(Arrays.stream(d.methods()).map(CompileCallable.class::cast).map(CompileCallable::convert).toArray(RuntimeCallable[]::new));
    }

    private Map<String, RuntimeField> convertFields(Map<String, CompileField> allFields) {
        return MapStream.of(allFields).mapValues(CompileField::convert).toMap();
    }

    @Override
    public ClassReference reference() {
        return CacheableClass.super.reference();
    }

    @Override
    public String toString() { //jesus
        return "GeneratedClass{" + name + "}[" +
                "methods=" + methods + ", " +
                "fields=" + allFields + ", " +
                "superclass=" + superclass + ']';
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
    public String name() {
        return this.name;
    }

    @Override
    public String absoluteName() {
        return CacheableClass.super.absoluteName();
    }

    @Override
    public String pck() {
        return this.packageRepresentation;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return this.superclass;
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return null;
    }

    @Override
    public AbstractMethodMap getMethods() {
        return methods;
    }

    @Override
    public Annotation[] annotations() {
        return this.annotations;
    }

    @Override
    public short getModifiers() {
        return 0;
    }

    @Override
    public boolean isNative() {
        return false;
    }
}