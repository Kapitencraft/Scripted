package net.kapitencraft.scripted.lang.holder.oop.generic;

import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.scripted.lang.holder.token.Token;

import java.util.HashMap;
import java.util.Map;

public record AppliedGenerics(Token reference, ClassReference[] references) {

    public void applyToStack(GenericStack stack, Generics reference, ErrorStorage logger) {
        if (reference.variables().length != this.references.length) {
            logger.error(this.reference, "Wrong number of type arguments: " + this.references.length + "; required: " + reference.variables().length);
        }
        Map<String, ClassReference> referenceMap = new HashMap<>();
        for (int i = 0; i < reference.variables().length; i++) {
            referenceMap.put(reference.variables()[i].name().lexeme(), references[i]);
        }
        stack.push(referenceMap);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AppliedGenerics appliedGenerics && referencesEqual(references, appliedGenerics.references);
    }

    private boolean referencesEqual(ClassReference[] expected, ClassReference[] gotten) {
        if (expected.length != gotten.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].get().isChildOf(gotten[i].get())) {
                return false;
            }
        }
        return true;
    }
}