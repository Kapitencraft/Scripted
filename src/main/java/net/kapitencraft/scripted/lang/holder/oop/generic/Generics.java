package net.kapitencraft.scripted.lang.holder.oop.generic;

import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.scripted.lang.holder.oop.Validatable;

import java.util.HashMap;
import java.util.Map;

public record Generics(Generic[] variables) implements Validatable {
    public void pushToStack(GenericStack stack) {
        Map<String, ClassReference> map = new HashMap<>();
        for (Generic generic : variables) map.put(generic.name().lexeme(), generic.reference());
        stack.push(map);
    }

    @Override
    public void validate(ErrorStorage logger) {
        for (Generic generic : variables) generic.validate(logger);
    }

    public boolean hasGeneric(String name) {
        for (Generic generic : variables) {
            if (name.equals(generic.name().lexeme())) return true;
        }
        return false;
    }

    public ClassReference getReference(String name) {
        for (Generic generic : variables) if (name.equals(generic.name().lexeme())) return generic.reference();
        return null;
    }
}