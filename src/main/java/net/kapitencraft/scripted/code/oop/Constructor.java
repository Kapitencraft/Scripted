package net.kapitencraft.scripted.code.oop;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class Constructor<T> extends Method<T> {
    private static final Pattern NAME_PATTERN = Pattern.compile("new([A-Z]\\w+)");
    protected Constructor(Consumer<ParamSet> params, String name) {
        super(params, name);
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("name '" + name + "' does not match pattern '" + NAME_PATTERN.pattern() + "'");
        }
    }

    public abstract Method<T>.Instance construct(JsonObject object, VarAnalyser analyser);
}