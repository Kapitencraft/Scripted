package net.kapitencraft.scripted.lang.exe.natives.impl;

import net.kapitencraft.scripted.lang.bytecode.exe.VirtualMachine;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.natives.NativeClassInstance;
import net.kapitencraft.scripted.lang.exe.natives.NativeClassLoader;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.PrimitiveClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NativeMethod implements ScriptedCallable {
    private final ClassReference type;
    private final ClassReference[] args;
    private final Method method;
    private final boolean instance;
    private final short modifiers;

    public NativeMethod(ClassReference type, ClassReference[] args, Method method, boolean instance, short modifiers) {
        this.type = type;
        this.args = args;
        this.method = method;
        this.instance = instance;
        this.modifiers = modifiers;
    }

    @Override
    public ClassReference retType() {
        return type;
    }

    @Override
    public ClassReference[] argTypes() {
        return args;
    }

    @Override
    public Object call(Object[] arguments) {
        try {
            Object o = method.invoke(instance ? NativeClassLoader.extractNative(arguments[0]) : null, NativeClassLoader.extractNatives(arguments, instance));
            if (this.type.get() instanceof PrimitiveClass) {
                return o;
            }
            return new NativeClassInstance((NativeClassImpl) this.type.get(), o);
        } catch (IllegalAccessException | InvocationTargetException e) {
            VirtualMachine.handleException(VirtualMachine.createException(VarTypeManager.FUNCTION_CALL_ERROR, e.getMessage()));
        } catch (Throwable e) {
            VirtualMachine.handleException(VirtualMachine.createException(VarTypeManager.UNKNOWN_ERROR, e.getMessage()));
        }
        return null;
    }

    @Override
    public boolean isAbstract() {
        return Modifiers.isAbstract(modifiers);
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(modifiers);
    }
}
