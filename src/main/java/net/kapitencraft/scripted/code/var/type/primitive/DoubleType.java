package net.kapitencraft.scripted.code.var.type.primitive;

import com.google.gson.JsonPrimitive;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.kapitencraft.scripted.init.VarTypes;
import org.jetbrains.annotations.NotNull;

public class DoubleType extends PrimitiveType<Double> {
    public DoubleType() {
        super("double", Double::sum, (d, d1) -> d * d1, (d, d1) -> d / d1, (d, d1) -> d - d1, (d, d1) -> d % d1, Double::compareTo);
    }

    public static MethodInstance<?> readInstance(String value) {
        return VarTypes.DOUBLE.get().readPrimitiveInstance(value);
    }

    @Override
    public @NotNull String toId() {
        return "D";
    }

    @Override
    public Double one() {
        return 1d;
    }

    @Override
    public Double negOne() {
        return -1d;
    }

    @Override
    public Double loadPrimitive(String string) {
        return Double.valueOf(string);
    }

    @Override
    public JsonPrimitive saveToJson(Double value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Double loadFromJson(JsonPrimitive object) {
        return object.getAsDouble();
    }
}
