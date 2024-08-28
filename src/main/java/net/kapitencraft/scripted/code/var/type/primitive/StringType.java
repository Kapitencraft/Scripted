package net.kapitencraft.scripted.code.var.type.primitive;

import com.google.gson.JsonPrimitive;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.kapitencraft.scripted.init.VarTypes;
import org.jetbrains.annotations.NotNull;

public class StringType extends PrimitiveType<String> {
    public StringType() {
        super("String", (s, s1) -> s + s1, null, null, null, null, String::compareTo);
    }

    public static MethodInstance<?> readInstance(String string) {
        return (VarTypes.STRING.get()).readPrimitiveInstance(string);
    }

    @Override
    public @NotNull String toId() {
        return "S";
    }

    @Override
    public String one() {
        return " ";
    }

    @Override
    public String negOne() {
        return "\n";
    }

    @Override
    public String loadPrimitive(String string) {
        return string;
    }

    @Override
    public JsonPrimitive saveToJson(String value) {
        return new JsonPrimitive(value);
    }

    @Override
    public String loadFromJson(JsonPrimitive prim) {
        return prim.getAsString();
    }
}
