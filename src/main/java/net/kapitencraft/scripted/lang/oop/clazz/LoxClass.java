package net.kapitencraft.scripted.lang.oop.clazz;

import net.kapitencraft.scripted.lang.VarTypeManager;
import net.kapitencraft.scripted.lang.env.core.Environment;
import net.kapitencraft.scripted.lang.func.LoxCallable;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.oop.ClassInstance;
import net.kapitencraft.scripted.lang.oop.LoxField;
import net.kapitencraft.scripted.lang.run.Interpreter;

import java.util.List;
import java.util.Map;

public interface LoxClass {

    String name();

    LoxClass superclass();

    default LoxClass getFieldType(String name) {
        return superclass().getFieldType(name);
    }

    LoxClass getStaticFieldType(String name);

    default boolean hasField(String name) {
        return superclass().hasField(name);
    }

    LoxClass getStaticMethodType(String name);

    default LoxClass getMethodType(String name) {
        return superclass().getMethodType(name);
    }

    LoxCallable getStaticMethod(String name);

    default LoxCallable getMethod(String name) {
        return superclass().getMethod(name);
    }

    default Map<String, LoxCallable> getAbstractMethods() {
        return superclass().getAbstractMethods();
    }

    default Map<String, LoxCallable> getMethods() {
        return superclass().getMethods();
    }

    boolean hasStaticMethod(String name);

    default boolean hasMethod(String name) {
        return superclass().hasMethod(name);
    }

    default ClassInstance createInst(List<Expr> params, Interpreter interpreter) {
        ClassInstance instance = new ClassInstance(this, interpreter);
        instance.construct(params, interpreter);
        return instance;
    }

    default Map<String, LoxField> getFields() {
        return superclass().getFields();
    }

    void callConstructor(Environment environment, Interpreter interpreter, List<Object> args);

    boolean isAbstract();

    default boolean is(LoxClass other) {
        return other == this;
    }

    default boolean isParentOf(LoxClass suspectedChild) {
        if (suspectedChild instanceof PreviewClass previewClass) suspectedChild = previewClass.getTarget();
        if (suspectedChild.is(this) || suspectedChild == VarTypeManager.OBJECT) return true;
        while (suspectedChild != VarTypeManager.OBJECT && !suspectedChild.is(this)) {
            suspectedChild = suspectedChild.superclass();
        }
        return suspectedChild.is(this);
    }

    default boolean isChildOf(LoxClass suspectedParent) {
        return suspectedParent.isParentOf(this);
    }
}