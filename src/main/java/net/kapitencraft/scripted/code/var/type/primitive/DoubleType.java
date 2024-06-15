package net.kapitencraft.scripted.code.var.type.primitive;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.regex.Pattern;

public class DoubleType extends PrimitiveType<Double> {
    public DoubleType() {
        super(Double::sum, (d, d1) -> d * d1, (d, d1) -> d / d1, (d, d1) -> d - d1, (d, d1) -> d % d1, d -> d);
    }

    @Override
    public Pattern matcher() {
        return PrimitiveType.NUMBER;
    }

    @Override
    public Double loadPrimitive(String string) {
        return Double.valueOf(string);
    }

    @Override
    public void saveToJson(JsonObject object, Double value) {
        object.addProperty("value", value);
    }

    @Override
    public Double loadFromJson(JsonObject object) {
        return GsonHelper.getAsDouble(object, "value");
    }
}
