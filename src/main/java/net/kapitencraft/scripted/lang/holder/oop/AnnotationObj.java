package net.kapitencraft.scripted.lang.holder.oop;

import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.token.Token;

public record AnnotationObj(SourceReference type, Token[] properties) implements Validatable {
    @Override
    public void validate(ErrorStorage logger) {
        type.validate(logger);
    }
}
