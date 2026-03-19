package net.kapitencraft.scripted.lang.oop.method.map;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationMethodMap implements AbstractMethodMap {
    private final Map<String, AnnotationCallable> methods;
    private final Map<String, DataMethodContainer> forLookup;
    private final List<String> abstracts;

    public AnnotationMethodMap(Map<String, AnnotationCallable> methods) {
        this.methods = methods;
        Map<String, DataMethodContainer> map = new HashMap<>();
        List<String> abstractMethodKeys = new ArrayList<>();
        methods.forEach((string, annotationCallable) -> {
            map.put(string, new DataMethodContainer(new ScriptedCallable[]{annotationCallable}));
            if (annotationCallable.isAbstract()) abstractMethodKeys.add(string);
        });
        abstracts = abstractMethodKeys;
        this.forLookup = ImmutableMap.copyOf(map);
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return methods.get(signature.substring(0, signature.length() - 2));
    }

    @Override
    public boolean has(String name) {
        return methods.containsKey(name);
    }

    @Override
    public Map<String, DataMethodContainer> asMap() {
        return forLookup;
    }

    @Override
    public @Nullable DataMethodContainer get(String name) {
        return forLookup.get(name);
    }

    protected Map<String, AnnotationCallable> getMethods() {
        return methods;
    }

    public List<String> getAbstracts() {
        return abstracts;
    }
}
