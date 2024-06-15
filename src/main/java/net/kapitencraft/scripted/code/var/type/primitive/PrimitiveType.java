package net.kapitencraft.scripted.code.var.type.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.var.VarType;

import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;

public abstract class PrimitiveType<T> extends VarType<T> {
    public PrimitiveType(BiFunction<T, T, T> add, BiFunction<T, T, T> mult, BiFunction<T, T, T> div, BiFunction<T, T, T> sub, BiFunction<T, T, T> mod, ToDoubleFunction<T> comp) {
        super(add, mult, div, sub, mod, comp);
    }
    public static final Pattern NUMBER = Pattern.compile("(\\d+)");
    public static final Pattern RESOURCE_LOCATION_MATCHER = Pattern.compile("(([a-z0-9._-]:)?[a-z0-9._-])");

    /**
     * @return the matcher to read an instance of this type from string (must contain exactly one capturing group)
     */
    public abstract Pattern matcher();

    public abstract T loadPrimitive(String string);

    public abstract void saveToJson(JsonObject object, T value);
    public abstract T loadFromJson(JsonObject object);
}
