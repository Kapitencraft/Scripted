package net.kapitencraft.scripted.lang.oop.clazz.skeleton;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.Holder;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.field.SkeletonField;
import net.kapitencraft.scripted.lang.oop.method.SkeletonMethod;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.scripted.lang.run.VarTypeManager;
import net.kapitencraft.scripted.lang.run.load.ClassLoader;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class SkeletonClass implements ScriptedClass {
    private final String name;
    private final String pck;

    private final String superclass;
    private final Map<String, SkeletonField> fields;
    private final Holder.EnumConstant[] constants;

    private final Holder.Generics generics;

    private final GeneratedMethodMap methods;

    private final short modifiers;
    private final String[] interfaces;
    private final boolean isCompiler; //this is necessary in order to use the correct mapper from string -> ClassReference

    public SkeletonClass(Holder.Generics generics,
                         String name, String pck, String superclass,
                         Map<String, SkeletonField> fields, Holder.EnumConstant[] constants,
                         Map<String, DataMethodContainer> methods,
                         short modifiers, String[] interfaces) {
        this.name = name;
        this.pck = pck;
        this.superclass = superclass;
        this.fields = fields;
        this.generics = generics;
        this.constants = constants;
        this.methods = new GeneratedMethodMap(methods);
        this.modifiers = modifiers;
        this.interfaces = interfaces;
        this.isCompiler = true;
    }

    public SkeletonClass(String name, String pck, String superclass,
                         Map<String, SkeletonField> fields,
                         Map<String, DataMethodContainer> methods,
                         short modifiers, String[] interfaces) {
        this.constants = null;
        this.generics = null;
        this.name = name;
        this.pck = pck;
        this.superclass = superclass;
        this.fields = fields;
        this.methods = new GeneratedMethodMap(methods);
        this.modifiers = modifiers;
        this.interfaces = interfaces;
        this.isCompiler = false;
    }

    public static SkeletonClass fromCache(JsonObject data, String pck) {
        String name = GsonHelper.getAsString(data, "name");
        String superclass = GsonHelper.getAsString(data, "superclass");

        String[] interfaces = ClassLoader.loadInterfaces(data);

        ImmutableMap<String, DataMethodContainer> methods = SkeletonMethod.readFromCache(data, "methods");

        ImmutableMap.Builder<String, SkeletonField> fields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(data, "fields");
            fieldData.asMap().forEach((s, element) -> {
                JsonObject object = element.getAsJsonObject();
                fields.put(s, new SkeletonField(VarTypeManager.directParseType(GsonHelper.getAsString(object, "type")), GsonHelper.getAsShort(object, "modifiers")));
            });
        }

        short modifiers = data.has("modifiers") ? GsonHelper.getAsShort(data, "modifiers") : 0;

        return new SkeletonClass(name, pck, superclass,
                fields.build(),
                methods,
                modifiers,
                interfaces
        );
    }

    @Override
    public Object getStaticField(String name) {
        throw new IllegalAccessError("cannot access field from skeleton");
    }

    @Override
    public Object setStaticField(String name, Object val) {
        throw new IllegalAccessError("cannot access field from skeleton");
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
    public boolean hasField(String name) {
        return this.fields.containsKey(name) || ScriptedClass.super.hasField(name);
    }

    @Override
    public ClassReference getFieldType(String name) {
        return Optional.ofNullable(this.fields.get(name)).map(SkeletonField::type).orElseGet(() -> superclass().get().getFieldType(name));
    }

    @Override
    public Map<String, SkeletonField> getFields() {
        return fields;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return isCompiler ? VarTypeManager.directParseTypeCompiler(superclass) : VarTypeManager.directParseType(superclass);
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return methods.getMethod(signature);
    }

    @Override
    public short getModifiers() {
        return modifiers;
    }

    @Override
    public boolean hasMethod(String name) {
        return methods.has(name) || ScriptedClass.super.hasMethod(name);
    }

    @Override
    public GeneratedMethodMap getMethods() {
        return methods;
    }

    @Override
    public Annotation[] annotations() {
        return new Annotation[0];
    }

    @Override
    public ClassReference[] interfaces() {
        return Arrays.stream(interfaces).map(isCompiler ? VarTypeManager::directParseTypeCompiler : VarTypeManager::directParseType).toArray(ClassReference[]::new);
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public Holder.EnumConstant getEnumConstant(String lexeme) {
        if (this.constants != null) {
            for (Holder.EnumConstant constant : this.constants) {
                if (constant.name().lexeme().equals(lexeme))
                    return constant;
            }
        }
        return null;
    }
}
