package net.kapitencraft.scripted.lang.exe.natives.impl;

import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;

@Deprecated
public abstract class NativeMethodImpl implements ScriptedCallable {
    private final ClassReference[] arguments;
    private final ClassReference retType;
    private final boolean isFinal, isAbstract;

    public NativeMethodImpl(ClassReference[] arguments, ClassReference retType, boolean isFinal, boolean isAbstract) {
        this.arguments = arguments;
        this.retType = retType;
        this.isFinal = isFinal;
        this.isAbstract = isAbstract;
    }

    @Override
    public ClassReference retType() {
        return retType;
    }

    @Override
    public ClassReference[] argTypes() {
        return arguments;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }
}
