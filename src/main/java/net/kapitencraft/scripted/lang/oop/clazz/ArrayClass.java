package net.kapitencraft.scripted.lang.oop.clazz;

public class ArrayClass extends PrimitiveClass {
    private final ScriptedClass component;

    public ArrayClass(ScriptedClass component) {
        super(component.name() + "[]", null);
        this.component = component;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean is(ScriptedClass other) {
        return other instanceof ArrayClass arrayClass ? arrayClass.component.is(this.component) :
                other instanceof PrimitiveClass primitiveClass && primitiveClass.is(this);
    }

    @Override
    public ScriptedClass getComponentType() {
        return component;
    }
}
