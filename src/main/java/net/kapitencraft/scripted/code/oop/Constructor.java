package net.kapitencraft.scripted.code.oop;

import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;

public abstract class Constructor<T> extends Method<T> {
    protected Constructor(ParamSet params, String name) {
        super(params, name);
    }

    public abstract Method<T>.Instance construct(ParamData data);
}
