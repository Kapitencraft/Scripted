package net.kapitencraft.scripted.lang.oop.method.annotation;

import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;

public class CompileAnnotationCallable extends AnnotationCallable {
    private final Expr expr;
    private final Annotation[] annotations;

    public CompileAnnotationCallable(ClassReference type, Expr expr, Annotation[] annotations) {
        super(type, null);
        this.expr = expr;
        this.annotations = annotations;
    }

    @Override
    public boolean isAbstract() {
        return expr == null;
    }

    @Override
    public Object call(Object[] arguments) {
        throw new IllegalAccessError("can not call compile annotation method");
    }
}
