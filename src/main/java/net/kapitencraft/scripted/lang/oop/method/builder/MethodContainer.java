package net.kapitencraft.scripted.lang.oop.method.builder;

import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;

public interface MethodContainer {

    ScriptedCallable getMethod(ClassReference[] expectedArgs);

    ScriptedCallable getMethodByOrdinal(int ordinal);

    int getMethodOrdinal(ClassReference[] types);

    ScriptedCallable[] methods();
}
