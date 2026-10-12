package net.kapitencraft.scripted.lang.holder.class_ref.generic;

import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.token.Token;

public class GenericSourceReference extends SourceReference {
    public GenericSourceReference(Token nameToken, ClassReference reference) {
        super(nameToken.lexeme(), nameToken, reference);
    }

    @Override
    public void validate(ErrorStorage logger) {
        //always exists
    }
}
