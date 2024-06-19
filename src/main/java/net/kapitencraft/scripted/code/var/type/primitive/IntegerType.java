package net.kapitencraft.scripted.code.var.type.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.minecraft.util.GsonHelper;

import java.util.regex.Pattern;

public class IntegerType extends PrimitiveType<Integer> {

    public IntegerType() {
        super("int", Integer::sum, (i, i1) -> i * i1, (i, i1) -> i / i1, (i, i1) -> i - i1, (i, i1) -> i % i1, Integer::compareTo);
    }

    @Override
    public Pattern matcher() {
        return PrimitiveType.NUMBER;
    }

    @Override
    public Integer loadPrimitive(String string) {
        return Integer.valueOf(string);
    }

    @Override
    public void saveToJson(JsonObject object, Integer value) {
        object.addProperty("value", value);
    }

    @Override
    public Integer loadFromJson(JsonObject object) {
        return GsonHelper.getAsInt(object, "value");
    }
}
