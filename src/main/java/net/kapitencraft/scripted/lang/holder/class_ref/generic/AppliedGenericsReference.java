package net.kapitencraft.scripted.lang.holder.class_ref.generic;

import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Holder;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import org.jetbrains.annotations.Nullable;

public class AppliedGenericsReference extends ClassReference {
    private final Holder.AppliedGenerics generics;
    private final ClassReference reference;

    public AppliedGenericsReference(ClassReference reference, Holder.AppliedGenerics generics) {
        super(reference.name(), reference.pck());
        this.generics = generics;
        this.reference = reference;
    }

    public static boolean genericsEqual(AppliedGenericsReference gotten, AppliedGenericsReference expected) {
        return expected.generics.equals(gotten.generics);
    }

    public void push(GenericStack genericStack, Compiler.ErrorStorage logger) {
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

    public Holder.AppliedGenerics getApplied() {
        return generics;
    }
}
