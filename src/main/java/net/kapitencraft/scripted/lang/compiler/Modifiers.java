package net.kapitencraft.scripted.lang.compiler;

public interface Modifiers {
    short STATIC = 1,
            FINAL = 2,
            INTERFACE = 4,
            ABSTRACT = 8,
            ANNOTATION = 16,
            ENUM = 32;
    //no access modifiers to worry about. yay!
    //short has 16 bits. should be sufficient

    static boolean isFinal(short modifiers) {
        return (modifiers & FINAL) != 0;
    }

    static boolean isStatic(short modifiers) {
        return (modifiers & STATIC) != 0;
    }

    static boolean isAbstract(short modifiers) {
        return (modifiers & ABSTRACT) != 0;
    }

    static short pack(boolean isFinal, boolean isStatic, boolean isAbstract) {
        return (short) ((isFinal ? FINAL : 0) | (isStatic ? STATIC : 0) | (isAbstract ? ABSTRACT : 0));
    }

    /**
     * extracts usable scripted modifiers from the given java modifiers.
     * <br> used for native class loading
     * @param javaMods the given java modifiers
     * @return the extracted scripted mods
     * @see java.lang.reflect.Modifier
     */
    static short fromJavaMods(int javaMods) {
        return (short) ((javaMods >> 3 & 3) | (javaMods >> 7 & 12) | (javaMods >> 9 & 48));
    }
}
