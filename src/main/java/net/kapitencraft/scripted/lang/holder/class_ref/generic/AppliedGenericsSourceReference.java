package net.kapitencraft.scripted.lang.holder.class_ref.generic;

import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.oop.generic.AppliedGenerics;
import net.kapitencraft.scripted.lang.holder.token.Token;

public class AppliedGenericsSourceReference extends SourceReference {
    private final AppliedGenerics generics;

    public static AppliedGenericsSourceReference create(Token nameToken, ClassReference type, AppliedGenerics generics) {
        return new AppliedGenericsSourceReference(type.name(), nameToken, type, generics);
    }

    protected AppliedGenericsSourceReference(String name, Token nameToken, ClassReference reference, AppliedGenerics generics) {
        super(name, nameToken, reference);
        this.generics = generics;
    }

    public AppliedGenerics getGenerics() {
        return generics;
    }
}
