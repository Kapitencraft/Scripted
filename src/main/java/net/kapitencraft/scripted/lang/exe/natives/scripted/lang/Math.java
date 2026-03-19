package net.kapitencraft.scripted.lang.exe.natives.scripted.lang;

import net.kapitencraft.scripted.lang.exe.natives.NativeClass;

@NativeClass(pck = "scripted.lang")
public class Math {

    public static int abs(int val) {
        return java.lang.Math.abs(val);
    }

    public static double abs(double val) {
        return java.lang.Math.abs(val);
    }

    public static float abs(float val) {
        return java.lang.Math.abs(val);
    }

    public static double sqrt(double val) {
        return java.lang.Math.sqrt(val);
    }

    public static double log(double val) {
        return java.lang.Math.log(val);
    }

    public static double log10(double val) {
        return java.lang.Math.log10(val);
    }

    public static double sin(double val) {
        return java.lang.Math.sin(val);
    }
}
