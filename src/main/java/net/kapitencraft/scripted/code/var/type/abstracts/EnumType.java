package net.kapitencraft.scripted.code.var.type.abstracts;

import com.google.gson.JsonPrimitive;
import net.kapitencraft.kap_lib.helpers.CollectorHelper;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class EnumType<T extends Enum<T>> extends PrimitiveType<T> {
    private final Class<T> tEnum;
    private final Map<Integer, T> nameMapper;

    public EnumType(String name, Class<T> tEnum) {
        super(name, null, null, null, null, null, Enum::compareTo);
        this.tEnum = tEnum;
        this.nameMapper = Arrays.stream(tEnum.getEnumConstants()).collect(CollectorHelper.createMapForKeys(T::ordinal));
    }


    @Override
    public T loadPrimitive(String string) {
        return Objects.requireNonNull(nameMapper.get(string), "unknown Enum constant for class '" + tEnum.getCanonicalName() + "': " + string);
    }

    @Override
    public JsonPrimitive saveToJson(T value) {
        return new JsonPrimitive(value.ordinal());
    }

    @Override
    public T loadFromJson(JsonPrimitive object) {
        return Objects.requireNonNull(nameMapper.get(object.getAsInt()), String.format("Unknown enum constant for ordinal %s in enum %s", object.getAsInt(), tEnum.getCanonicalName()));
    }
}
