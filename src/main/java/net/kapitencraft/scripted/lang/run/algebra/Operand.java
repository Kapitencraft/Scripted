package net.kapitencraft.scripted.lang.run.algebra;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

public enum Operand {
    LEFT,
    RIGHT;

    public static Operand fromJson(JsonObject object, String name) {
        return Operand.valueOf(GsonHelper.getAsString(object, name));
    }
}
