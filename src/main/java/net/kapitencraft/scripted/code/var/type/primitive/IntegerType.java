package net.kapitencraft.scripted.code.var.type.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.util.GsonHelper;

import java.util.regex.Pattern;

public class IntegerType extends PrimitiveType<Integer> {

    public IntegerType() {
        super("int", Integer::sum, (i, i1) -> i * i1, (i, i1) -> i / i1, (i, i1) -> i - i1, (i, i1) -> i % i1, Integer::compareTo);
    }

    public static Method<?>.Instance readInstance(String value) {
        return ((IntegerType) VarTypes.INTEGER.get()).loadPrimitiveInstance(value);
    }

    @Override
    public Integer one() {
        return 1;
    }

    @Override
    public Integer negOne() {
        return -1;
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
