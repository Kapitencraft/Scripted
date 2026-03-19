package net.kapitencraft.scripted.lang.oop.method.builder;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.compiler.CacheBuilder;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.oop.method.RuntimeCallable;
import net.kapitencraft.scripted.lang.tool.Util;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record DataMethodContainer(ScriptedCallable[] methods) implements MethodContainer {

    public static DataMethodContainer of(ScriptedCallable... methods) {
        return new DataMethodContainer(methods);
    }

    public ScriptedCallable getMethod(ClassReference[] expectedArgs) {
        for (ScriptedCallable method : methods) {
            if (Util.matchArgs(method.argTypes(), expectedArgs)) return method;
        }
        //just going to make it easier for myself xD
        return methods[0];
    }

    public static ImmutableMap<String, DataMethodContainer> load(JsonObject data, String className, String member) {
        ImmutableMap.Builder<String, DataMethodContainer> methods = new ImmutableMap.Builder<>();
        {
            JsonObject methodData = GsonHelper.getAsJsonObject(data, member);
            methodData.asMap().forEach((name1, element) -> {
                try {
                    DataMethodContainer container = new DataMethodContainer(element.getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).map(RuntimeCallable::load).toArray(ScriptedCallable[]::new));
                    methods.put(name1, container);
                } catch (Exception e) {
                    System.err.printf("error loading method '%s' inside class '%s': %s%n", name1, className, e.getMessage());
                }
            });
        }
        return methods.build();
    }

    //expecting Container methods to be Functions (!)
    public JsonArray cache(CacheBuilder builder) {
        JsonArray array = new JsonArray(methods.length);
        for (ScriptedCallable method : methods) {
            if (method instanceof CompileCallable function) {
                array.add(function.save(builder));
            }
        }
        return array;
    }

    public ScriptedCallable getMethodByOrdinal(int ordinal) {
        if (ordinal == -1) return methods[0]; //default
        return methods[ordinal];
    }

    public int getMethodOrdinal(ClassReference[] types) {
        for (int i = 0; i < methods.length; i++) {
            if (Util.matchArgs(types, methods[i].argTypes())) return i;
        }
        return -1;
    }

    public static class Builder {
        private final Token className;
        private final List<ScriptedCallable> methods = new ArrayList<>();

        public Builder(Token className) {
            this.className = className;
        }

        public void addMethod(Compiler.ErrorStorage errorStorage, ScriptedCallable callable, Token methodName) {
            List<? extends ClassReference[]> appliedTypes = methods.stream().map(ScriptedCallable::argTypes).toList();
            ClassReference[] argTypes = callable.argTypes();
            for (ClassReference[] appliedType : appliedTypes) {
                if (Util.matchArgs(argTypes, appliedType)) {
                    errorStorage.errorF(methodName, "method %s(%s) is already defined in class %s", methodName.lexeme(), Util.getDescriptor(argTypes), className.lexeme());
                    return;
                }
            }
            methods.add(callable);
        }

        public DataMethodContainer create() {
            return new DataMethodContainer(methods.toArray(new ScriptedCallable[0]));
        }
    }

    public static Map<String, DataMethodContainer> bakeBuilders(Map<String, Builder> builders) {
        Map<String, DataMethodContainer> map = new HashMap<>();
        builders.forEach((name, builder) -> map.put(name, builder.create()));
        return ImmutableMap.copyOf(map);
    }
}
