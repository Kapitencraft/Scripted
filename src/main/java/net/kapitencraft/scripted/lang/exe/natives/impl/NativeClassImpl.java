package net.kapitencraft.scripted.lang.exe.natives.impl;

import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.MethodLookup;
import net.kapitencraft.scripted.lang.exe.natives.NativeClassLoader;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.field.NativeField;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.oop.method.map.GeneratedMethodMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ApiStatus.Internal
public class NativeClassImpl implements ScriptedClass {
    private final GeneratedMethodMap methods;
    private final MethodLookup lookup;
    private final Map<String, NativeField> fields;
    private final ClassReference superclass;
    private final ClassReference[] interfaces;
    private final ResourceKey<? extends Registry<?>> owner;
    private final short modifiers;
    private final String name, pck;

    @ApiStatus.Internal
    public NativeClassImpl(String name, String pck,
                           Map<String, DataMethodContainer> methods, Map<String, NativeField> fields,
                           ClassReference superclass, ClassReference[] interfaces, ResourceKey<? extends Registry<?>> owner, short modifiers) {
        this.name = name;
        this.pck = pck;
        this.methods = new GeneratedMethodMap(methods);
        this.fields = fields;
        this.superclass = superclass;
        this.interfaces = interfaces;
        this.owner = owner;
        this.modifiers = modifiers;
        this.lookup = new MethodLookup(this);
    }

    @Nullable
    public ResourceKey<? extends Registry<?>> getOwner() {
        return owner;
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
        return superclass;
    }

    @Override
    public ClassReference getFieldType(String name) {
        return fields.containsKey(name) ? fields.get(name).type() : null;
    }

    @Override
    public boolean hasField(String name) {
        return fields.containsKey(name);
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return lookup.get(signature);
    }

    @Override
    public short getModifiers() {
        return modifiers;
    }

    @Override
    public boolean hasMethod(String name) {
        return methods.has(name) || superclass != null && superclass.get().hasMethod(name);
    }

    @Override
    public Map<String, NativeField> getFields() {
        return fields;
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
        return interfaces;
    }

    @Override
    public Object getStaticField(String name) {
        return fields.get(name).get(null);
    }

    @Override
    public Object setStaticField(String name, Object val) {
        fields.get(name).set(null, NativeClassLoader.extractNative(val));
        return val;
    }

    @Override
    public Object staticSpecialAssign(String name, TokenType assignType) {
        return ScriptedClass.super.staticSpecialAssign(name, assignType);
    }

    @Override
    public boolean isNative() {
        return true;
    }
}
