package net.kapitencraft.scripted.lang.oop.method.map;

import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface AbstractMethodMap {

    ScriptedCallable getMethod(String signature);

    boolean has(String name);

    Map<String, DataMethodContainer> asMap();

    @Nullable DataMethodContainer get(String name);
}
