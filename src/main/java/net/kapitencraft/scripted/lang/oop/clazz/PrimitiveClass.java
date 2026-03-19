package net.kapitencraft.scripted.lang.oop.clazz;

import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.PrimitiveClassReference;
import net.kapitencraft.scripted.lang.oop.field.ScriptedField;
import net.kapitencraft.scripted.lang.oop.method.map.GeneratedMethodMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ApiStatus.Internal
public abstract class PrimitiveClass implements ScriptedClass {
    private final String name;
    private final ClassReference superclass;
    private final Object defaultValue;
    private final PrimitiveClassReference reference;

    public PrimitiveClass(ScriptedClass superclass, String name, Object defaultValue) {
        this.name = name;
        this.superclass = superclass != null ? superclass.reference() : null;
        this.defaultValue = defaultValue;
        this.reference = new PrimitiveClassReference(this.name, this);
    }

    public PrimitiveClass(String name, Object defaultValue) {
        this(null, name, defaultValue);
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
        return name;
    }

    @Override
    public String toString() {
        return "Primitive$" + name;
    }

    @Override
    public String pck() {
        return "scripted.lang";
    }

    @Override
    public @Nullable ClassReference superclass() {
        return superclass;
    }

    @Override
    public ClassReference getFieldType(String name) {
        return null;
    }

    @Override
    public boolean hasField(String name) {
        return false;
    }

    @Override
    public boolean hasMethod(String name) {
        return false;
    }

    @Override
    public Map<String, ? extends ScriptedField> getFields() {
        return Map.of();
    }

    @Override
    public short getModifiers() {
        return 0;
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return null;
    }

    @Override
    public GeneratedMethodMap getMethods() {
        return null;
    }

    public Object defaultValue() {
        return defaultValue;
    }

    @Override
    public ClassReference[] interfaces() {
        return new ClassReference[0];
    }

    @Override
    public ClassReference reference() {
        return reference;
    }

    @Override
    public boolean isNative() {
        return true;
    }

    @Override
    public Annotation[] annotations() {
        return new Annotation[0];
    }
}
