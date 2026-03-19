package net.kapitencraft.scripted.lang.oop.clazz.inst;

import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;

import java.util.HashMap;
import java.util.Map;

public class DynamicClassInstance implements ClassInstance {
    private final Map<String, Object> fields = new HashMap<>();
    private final ScriptedClass type;

    public ScriptedClass getType() {
        return type;
    }

    public DynamicClassInstance(ScriptedClass type) {
        this.type = type;
    }

    public void assignField(String name, Object val) {
        this.fields.put(name, val);
    }

    public Object getField(String name) {
        return this.fields.get(name);
    }

    @Override
    public String toString() {
        return "Dynamic{" + this.type.absoluteName() + "}"; //": {fields=" + this.fields + "}";
    }
}
