package net.kapitencraft.scripted.lang.oop.clazz;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.compiler.CacheBuilder;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.run.VarTypeManager;

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
