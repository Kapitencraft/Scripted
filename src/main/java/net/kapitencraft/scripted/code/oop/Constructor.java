package net.kapitencraft.scripted.code.oop;

import net.kapitencraft.scripted.code.method.Method;
import net.kapitencraft.scripted.code.method.param.ParamData;
import net.kapitencraft.scripted.code.method.param.ParamSet;

public abstract class Constructor<T> extends Method<T> {
    protected Constructor(ParamSet params, String name) {
        super(params, name);
    }
}
