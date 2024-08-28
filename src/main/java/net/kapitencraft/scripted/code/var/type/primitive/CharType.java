package net.kapitencraft.scripted.code.var.type.primitive;

import com.google.gson.JsonPrimitive;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.kapitencraft.scripted.init.VarTypes;
import org.jetbrains.annotations.NotNull;

public class CharType extends PrimitiveType<Character> {

    public CharType() {
        super("char", null, null, null, null, null, Character::compareTo);
    }

    public static MethodInstance<?> read(char value) {
        return VarTypes.CHAR.get().readPrimitiveInstance(String.valueOf(value));
    }

    @Override
    public @NotNull String toId() {
        return "C";
    }

    @Override
    public Character loadPrimitive(String string) {
        return string.charAt(0);
    }

    @Override
    public JsonPrimitive saveToJson(Character value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Character loadFromJson(JsonPrimitive prim) {
        return prim.getAsCharacter();
    }
}
