package net.kapitencraft.scripted.lang.oop.clazz;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.compiler.CacheBuilder;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;

public interface CacheableClass {

    JsonObject save(CacheBuilder cacheBuilder);

    default ClassReference reference() {
        return VarTypeManager.getOrCreateClass(name(), pck());
    }

    String pck();

    String name();

    default String absoluteName() {
        return pck() + "." + name();
    }
}
