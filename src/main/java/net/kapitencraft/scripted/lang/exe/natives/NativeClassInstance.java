package net.kapitencraft.scripted.lang.exe.natives;

import net.kapitencraft.scripted.lang.exe.natives.impl.NativeClassImpl;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.clazz.inst.ClassInstance;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NativeClassInstance implements ClassInstance {
    private final Object obj;
    private final NativeClassImpl type;

    public NativeClassInstance(NativeClassImpl type, Object obj) {
        this.obj = obj;
        this.type = type;
    }

    public Object getObject() {
        return obj;
    }

    @Override
    public String toString() {
        return obj.toString();
    }

    @Override
    public void assignField(String name, Object val) {
        type.getFields().get(name).set(obj, NativeClassLoader.extractNative(val));
    }

    @Override
    public Object getField(String name) {
        return type.getFields().get(name).get(obj);
    }

    @Override
    public ScriptedClass getType() {
        return type;
    }
}
