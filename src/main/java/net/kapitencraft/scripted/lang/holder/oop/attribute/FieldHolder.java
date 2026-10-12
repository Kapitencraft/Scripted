package net.kapitencraft.scripted.lang.holder.oop.attribute;


import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.Validatable;
import net.kapitencraft.scripted.lang.holder.token.Token;

public record FieldHolder(short modifiers, AnnotationObj[] annotations, SourceReference type, Token name, Token assign,
                          Token[] body) implements Validatable {
    @Override
    public void validate(ErrorStorage logger) {
        Validatable.validateNullable(annotations, logger);
        type.validate(logger);
    }
}
