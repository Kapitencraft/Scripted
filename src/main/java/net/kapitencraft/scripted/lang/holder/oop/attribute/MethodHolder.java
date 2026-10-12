package net.kapitencraft.scripted.lang.holder.oop.attribute;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.Validatable;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generics;
import net.kapitencraft.scripted.lang.holder.token.Token;

import java.util.List;

public record MethodHolder(short modifiers,
                           AnnotationObj[] annotations, Generics generics, SourceReference returnType,
                           Token name, Token closeBracket, List<Pair<SourceReference, String>> params,
                           List<SourceReference> thrown,
                           Token[] body) implements Validatable, OperationHolder {
    public void validate(ErrorStorage logger) {
        Validatable.validateNullable(annotations, logger);
        returnType.validate(logger);
        params.forEach(p -> p.getFirst().validate(logger));
        thrown.forEach(s -> s.validate(logger));
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(modifiers);
    }

    @Override
    public ClassReference retType() {
        return returnType.getReference();
    }
}
