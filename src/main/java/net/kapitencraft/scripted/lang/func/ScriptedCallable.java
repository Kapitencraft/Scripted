package net.kapitencraft.scripted.lang.func;

import net.kapitencraft.scripted.lang.bytecode.storage.Chunk;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;

import java.util.HashMap;
import java.util.Map;

public interface ScriptedCallable {

    static Map<String, ScriptedCallable> parseMethods(Map<String, DataMethodContainer> methods) {
        Map<String, ScriptedCallable> map = new HashMap<>();
        methods.forEach((string, dataMethodContainer) -> {
            for (ScriptedCallable method : dataMethodContainer.methods()) {
                map.put(VarTypeManager.getMethodSignatureNoTarget(string, method.argTypes()), method);
            }
        });
        return map;
    }

    ClassReference retType();

    ClassReference[] argTypes();

    Object call(Object[] arguments);

    default Chunk getChunk() {
        return null;
    }

    boolean isAbstract();

    boolean isFinal();

    boolean isStatic();

    default boolean isNative() {
        return true;
    }
}