package net.kapitencraft.scripted.lang.run.natives;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * overwrites the name of the given native field / method
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Rename {

    /**
     * @return the new name
     */
    String value();
}
