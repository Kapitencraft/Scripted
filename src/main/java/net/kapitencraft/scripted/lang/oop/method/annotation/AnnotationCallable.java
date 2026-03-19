package net.kapitencraft.scripted.lang.oop.method.annotation;

import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import org.jetbrains.annotations.Nullable;

public class AnnotationCallable implements ScriptedCallable {
    private final ClassReference type;
    private final @Nullable Object value;

    public AnnotationCallable(ClassReference type, @Nullable Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public ClassReference retType() {
        return type;
    }

    protected Object value() {
        return value;
    }

    @Override
    public ClassReference[] argTypes() {
        return new ClassReference[0];
    }

    @Override
    public Object call(Object[] arguments) {
        return value;
    }

    @Override
    public boolean isAbstract() {
        return value == null;
    }

    @Override
    public boolean isFinal() {
        return true;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isNative() {
        return true;
    }
}