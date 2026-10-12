package net.kapitencraft.scripted.lang.func;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;
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

    /**
     * supplies the given return tpe
     * @return the return type of this Method / Function
     */
    ClassReference retType();

    /**
     * the argument / parameter types this method declares in order they are declared in
     * @return the argument types this method declares
     */
    ClassReference[] argTypes();

    /**
     * the class types this method may throw
     * @return the class types this method might throw
     */
    ClassReference[] thrown();

    /**
     * @param arguments the arguments to call this method. only used in Native Methods
     * @return the return value this method call returns
     */
    Object call(Object[] arguments);

    /**
     * @return the code implementing this method
     */
    default Chunk getChunk() {
        return null;
    }

    /**
     * whether this method is abstract, and can therefore not be called directly
     * @return whether this method is abstract
     */
    boolean isAbstract();

    /**
     * whether this method is final, and can therefore not be overwritten
     * @return whether this method is final
     */
    boolean isFinal();

    /**
     * whether this method is static, and therefore doesn't require an instance of the declaring class to be invoked
     * @return whether this method is static
     */
    boolean isStatic();

    /**
     * whether this method is native, and is therefore executed directly in the Java VM, instead of the Scripted VM
     * @return whether this method is native
     */
    default boolean isNative() {
        return true;
    }
}