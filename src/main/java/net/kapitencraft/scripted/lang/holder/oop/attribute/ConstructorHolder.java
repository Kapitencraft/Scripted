package net.kapitencraft.scripted.lang.holder.oop.attribute;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.Validatable;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generics;
import net.kapitencraft.scripted.lang.holder.token.Token;

import java.util.List;

public record ConstructorHolder(AnnotationObj[] annotations, Generics generics, Token name, Token closeBracket,
                                List<Pair<SourceReference, String>> params, List<SourceReference> thrown,
                                Token[] body) implements OperationHolder {
    public void validate(ErrorStorage logger) {
        Validatable.validateNullable(annotations, logger);
        params.forEach(p -> p.getFirst().validate(logger));
        thrown.forEach(s -> s.validate(logger));
    }

    @Override
    public ClassReference retType() {
        return VarTypeManager.VOID.reference();
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public short modifiers() {
        return 0;
    }
}
