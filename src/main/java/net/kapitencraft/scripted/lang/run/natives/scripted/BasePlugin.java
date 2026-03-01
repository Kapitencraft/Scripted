package net.kapitencraft.scripted.lang.run.natives.scripted;

import net.kapitencraft.scripted.lang.run.natives.ClassRegistration;
import net.kapitencraft.scripted.lang.run.natives.ScriptedPlugin;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@ScriptedPlugin
public class BasePlugin {

    public static void registerClasses(ClassRegistration registration) {
        registration.registerClass(Object.class, "scripted.lang", "Object", "toString", "equals");
        registration.registerClass(Error.class, "scripted.lang");
        registration.registerClass(Override.class, "scripted.lang");
        registration.registerClass(RuntimeException.class, "scripted.lang");
        registration.registerClass(IndexOutOfBoundsException.class, "scripted.lang");
        registration.registerClass(ArithmeticException.class, "scripted.lang");
        registration.registerClass(VirtualMachineError.class, "scripted.lang");
        registration.registerClass(StackOverflowError.class, "scripted.lang");
        registration.registerClass(Annotation.class, "scripted.lang.annotations");
        registration.registerClass(String.class, "scripted.lang", "String");
        registration.registerClass(Throwable.class, "scripted.lang");
        registration.registerClass(Exception.class, "scripted.lang");
        registration.registerClass(Retention.class, "scripted.lang.annotations");
        registration.registerClass(RetentionPolicy.class, "scripted.lang.annotations");
        registration.registerClass(Math.class, "scripted.lang");
    }
}
