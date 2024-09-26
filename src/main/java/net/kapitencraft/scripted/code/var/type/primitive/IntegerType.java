package net.kapitencraft.scripted.code.var.type.primitive;

import com.google.gson.JsonPrimitive;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.kapitencraft.scripted.init.VarTypes;
import org.jetbrains.annotations.NotNull;

public class IntegerType extends PrimitiveType<Integer> {

    public IntegerType() {
        super("int", Integer::sum, (i, i1) -> i * i1, (i, i1) -> i / i1, (i, i1) -> i - i1, (i, i1) -> i % i1, Integer::compareTo);
    }

    @Override
    public Class<Integer> getTypeClass() {
        return int.class;
    }

    public static MethodInstance<?> readInstance(String value) {
        return VarTypes.INTEGER.get().readPrimitiveInstance(value);
    }

    @Override
    public @NotNull String toId() {
        return "I";
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
    public Integer loadPrimitive(String string) {
        return Integer.valueOf(string);
    }

    @Override
    public JsonPrimitive saveToJson(Integer value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Integer loadFromJson(JsonPrimitive prim) {
        return prim.getAsInt();
    }
}
