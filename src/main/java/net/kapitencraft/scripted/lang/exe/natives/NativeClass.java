package net.kapitencraft.scripted.lang.exe.natives;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeClass {

    /**
     * @return the name of the class. leave empty to use the class name
     */
    String name() default "";

    /**
     * @return the scripted package to add this class to
     */
    String pck();
}