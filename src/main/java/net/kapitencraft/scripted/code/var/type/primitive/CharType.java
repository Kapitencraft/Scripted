package net.kapitencraft.scripted.code.var.type.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.util.GsonHelper;

import java.util.regex.Pattern;

public class CharType extends PrimitiveType<Character> {
    public static final Pattern PATTERN = Pattern.compile("^('(.)')$");

    public CharType() {
        super("char", null, null, null, null, null, Character::compareTo);
    }

    public static Method<?>.Instance read(char value) {
        return ((CharType) VarTypes.CHAR.get()).loadPrimitiveInstance(String.valueOf(value));
    }

    @Override
    public Pattern matcher() {
        return PATTERN;
    }

    @Override
    public Character loadPrimitive(String string) {
        return string.charAt(0);
    }

    @Override
    public void saveToJson(JsonObject object, Character value) {
        object.addProperty("value", value);
    }

    @Override
    public Character loadFromJson(JsonObject object) {
        return GsonHelper.getAsCharacter(object, "value");
    }
}
