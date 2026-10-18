package net.kapitencraft.scripted.lang.compiler;

import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.oop.Validatable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class VarTypeContainer implements Validatable {
    private final Map<String, SourceReference> implemented = new HashMap<>();

    public VarTypeContainer() {
        implemented.putAll(VarTypeManager.getPackage("scripted.lang").allClasses().stream().collect(Collectors.toMap(ClassReference::name, SourceReference::createNative)));
    }

    public boolean hasClass(String clazz) {
        return implemented.containsKey(clazz);
    }

    public ClassReference getClass(String clazz) {
        SourceReference reference = implemented.get(clazz);
        if (reference == null)
            return null;
        return reference.getReference();
    }

    public void addClass(SourceReference clazz, @Nullable String nameOverride) {
        implemented.put(nameOverride != null ? nameOverride : clazz.getReference().name(), clazz);
    }

    @Override
    public String toString() {
        return "VarTypeParser" + implemented;
    }

    public boolean hasClass(ClassReference target, String nameOverride) {
        return hasClass(Optional.ofNullable(nameOverride).orElseGet(target::name));
    }

    public void validate(ErrorStorage logger) {
        implemented.values().forEach(ref -> ref.validate(logger));
    }
}
