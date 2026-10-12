package net.kapitencraft.scripted.lang.holder.class_ref.generic;

import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.oop.generic.AppliedGenerics;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import org.jetbrains.annotations.Nullable;

public class AppliedGenericsReference extends ClassReference {
    private final AppliedGenerics generics;
    private final ClassReference reference;

    public AppliedGenericsReference(ClassReference reference, AppliedGenerics generics) {
        super(reference.name(), reference.pck());
        this.generics = generics;
        this.reference = reference;
    }

    public static boolean genericsEqual(AppliedGenericsReference gotten, AppliedGenericsReference expected) {
        return expected.generics.equals(gotten.generics);
    }

    public void push(GenericStack genericStack, ErrorStorage logger) {
        this.generics.applyToStack(genericStack, this.reference.getGenerics(), logger);
    }

    @Override
    public boolean exists() {
        return reference.exists();
    }

    @Override
    public boolean is(ScriptedClass scriptedClass) {
        return scriptedClass instanceof AppliedGenericsReference aGR && aGR.reference.is(this.get()) && aGR.generics.equals(this.generics);
    }

    @Override
    public ScriptedClass get(@Nullable GenericStack generics) {
        return reference.get(generics);
    }

    public AppliedGenerics getApplied() {
        return generics;
    }
}
