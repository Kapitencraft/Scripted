package net.kapitencraft.scripted.lang.oop.method.map;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.compiler.CacheBuilder;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GeneratedMethodMap implements AbstractMethodMap {
    private final Map<String, DataMethodContainer> methods;
    private final Map<String, ScriptedCallable> plainMap;

    public GeneratedMethodMap(Map<String, DataMethodContainer> methods) {
        this.methods = methods;
        this.plainMap = ScriptedCallable.parseMethods(methods);
    }

    public static GeneratedMethodMap empty() {
        return new GeneratedMethodMap(Map.of());
    }

    public JsonObject save(CacheBuilder builder) {
        JsonObject methods = new JsonObject();
        this.methods.forEach((name, container) -> methods.add(name, container.cache(builder)));
        return methods;
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return plainMap.get(signature);
    }

    public boolean has(String name) {
        return methods.containsKey(name);
    }

    public Map<String, DataMethodContainer> asMap() {
        return methods;
    }

    @Override
    public @Nullable DataMethodContainer get(String name) {
        return methods.get(name);
    }
}
