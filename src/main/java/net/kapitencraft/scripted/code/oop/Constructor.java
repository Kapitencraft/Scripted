package net.kapitencraft.scripted.code.oop;

import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;

import java.util.regex.Pattern;

public abstract class Constructor<T> extends Method<T> {
    private static final Pattern NAME_PATTERN = Pattern.compile("new([A-Z][a-zA-Z])");
    protected Constructor(ParamSet params, String name) {
        super(params, name);
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("name '" + name + "' does not match pattern '" + NAME_PATTERN.pattern() + "'");
        }
    }

    public abstract Method<T>.Instance construct(ParamData data);
}
