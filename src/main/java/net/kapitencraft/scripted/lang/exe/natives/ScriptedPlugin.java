package net.kapitencraft.scripted.lang.exe.natives;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * indicates that the given class should work as a plugin for scripted.
 * <br> plugins can register different methods to interact with the registration.
 * <br> all these methods <i>must</i> be {@code public static}
 * <br>{@code void registerClasses(ClassRegistration)} used to register classes
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScriptedPlugin {
}
