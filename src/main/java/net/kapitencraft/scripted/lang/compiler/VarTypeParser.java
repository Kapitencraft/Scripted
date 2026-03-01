package net.kapitencraft.scripted.lang.compiler;

import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.scripted.lang.run.VarTypeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class VarTypeParser implements Holder.Validateable {
    private final Map<String, SourceClassReference> implemented = new HashMap<>();

    public VarTypeParser() {
        implemented.putAll(VarTypeManager.getPackage("scripted.lang").allClasses().stream().collect(Collectors.toMap(ClassReference::name, scriptedClass -> SourceClassReference.from(null, scriptedClass))));
    }

    public boolean hasClass(String clazz) {
        return implemented.containsKey(clazz);
    }

    public ClassReference getClass(String clazz) {
        SourceClassReference reference = implemented.get(clazz);
        if (reference == null)
            return null;
        return reference.getReference();
    }

    public void addClass(SourceClassReference clazz, String nameOverride) {
        implemented.put(nameOverride != null ? nameOverride : clazz.getReference().name(), clazz);
    }

    @Override
    public String toString() {
        return "VarTypeParser" + implemented;
    }

    public boolean hasClass(ClassReference target, String nameOverride) {
        return hasClass(Optional.ofNullable(nameOverride).orElseGet(target::name));
    }

    public void validate(Compiler.ErrorStorage logger) {
        implemented.values().forEach(ref -> ref.validate(logger));
    }
}
