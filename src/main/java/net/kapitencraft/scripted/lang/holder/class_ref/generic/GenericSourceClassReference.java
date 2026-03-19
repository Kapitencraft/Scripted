package net.kapitencraft.scripted.lang.holder.class_ref.generic;

import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;

public class GenericSourceClassReference extends SourceClassReference {
    public GenericSourceClassReference(Token nameToken, ClassReference reference) {
        super(nameToken.lexeme(), nameToken, reference);
    }

    @Override
    public void validate(Compiler.ErrorStorage logger) {
        //always exists
    }
}
