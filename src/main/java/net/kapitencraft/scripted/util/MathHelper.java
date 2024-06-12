package net.kapitencraft.scripted.util;

import net.minecraft.util.FastColor;
import org.joml.Vector4f;

public interface MathHelper {
    static Vector4f getColor(int pColor) {
        float f3 = (float) FastColor.ARGB32.alpha(pColor) / 255.0F;
        float f = (float)FastColor.ARGB32.red(pColor) / 255.0F;
        float f1 = (float)FastColor.ARGB32.green(pColor) / 255.0F;
        float f2 = (float)FastColor.ARGB32.blue(pColor) / 255.0F;
        return new Vector4f(f, f1, f2, f3);
    }
}
