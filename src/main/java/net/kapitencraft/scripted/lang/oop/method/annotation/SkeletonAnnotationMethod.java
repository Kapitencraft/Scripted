package net.kapitencraft.scripted.lang.oop.method.annotation;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.run.load.ClassLoader;

public class SkeletonAnnotationMethod extends AnnotationCallable {
    private final boolean hasValue;
    
    public SkeletonAnnotationMethod(ClassReference type, boolean hasValue) {
        super(type, null);
        this.hasValue = hasValue;
    }

    public static SkeletonAnnotationMethod fromJson(JsonObject object) {
        ClassReference type = ClassLoader.loadClassReference(object, "type");
        boolean hasValue = object.has("val");
        return new SkeletonAnnotationMethod(type, hasValue);
    }

    @Override
    public boolean isAbstract() {
        return !hasValue;
    }
}
