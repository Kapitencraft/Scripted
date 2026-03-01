package net.kapitencraft.scripted.lang.holder.class_ref.generic;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;

public class GenericClassReference extends ClassReference {
    private final ClassReference lowerBound, upperBound;
    private final String name;

    public GenericClassReference(String name, ClassReference lowerBound, ClassReference upperBound) {
        super(name, "");
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public String absoluteName() {
        return "?" + (lowerBound != null ? " extends " + lowerBound.absoluteName() : "") + (upperBound != null ? " super " + upperBound.absoluteName() : "");
    }

    @Override
    public String name() {
        return absoluteName();
    }

    public String getTypeName() {
        return name;
    }

    @Override
    public boolean exists() {
        return lowerBound == null || lowerBound.exists() && upperBound == null || upperBound.exists();
    }

    @Override
    public ScriptedClass get(GenericStack generics) {
        return generics == null ?
                lowerBound == null ?
                        VarTypeManager.OBJECT.get() :
                        lowerBound.get() :
                unwrap(generics).get(generics);
    }

    public ClassReference unwrap(GenericStack genericStack) {
        ClassReference reference = genericStack.getValue(name).orElse(
                lowerBound == null ?
                        VarTypeManager.OBJECT :
                        lowerBound
        );
        while (reference instanceof GenericClassReference gCR) {
            reference = gCR.unwrap(genericStack);
        }
        return reference;
    }
}
