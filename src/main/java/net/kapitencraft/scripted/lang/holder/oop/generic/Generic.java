package net.kapitencraft.scripted.lang.holder.oop.generic;

import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericClassReference;
import net.kapitencraft.scripted.lang.holder.oop.Validatable;
import net.kapitencraft.scripted.lang.holder.token.Token;

import java.util.Optional;

public record Generic(Token name, SourceReference lowerBound, SourceReference upperBound,
                      GenericClassReference reference) implements Validatable {

    public Generic(Token name, SourceReference lowerBound, SourceReference upperBound) {
        this(name, lowerBound, upperBound,
                new GenericClassReference(name.lexeme(),
                        Optional.ofNullable(lowerBound).map(SourceReference::getReference).orElse(null),
                        Optional.ofNullable(upperBound).map(SourceReference::getReference).orElse(null)
                )
        );
    }

    @Override
    public void validate(ErrorStorage logger) {
        if (lowerBound != null) lowerBound.validate(logger);
        if (upperBound != null) upperBound.validate(logger);
    }
}
