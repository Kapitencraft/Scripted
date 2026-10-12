package net.kapitencraft.scripted.lang.exe.natives.scripted.lang;

import net.kapitencraft.scripted.lang.exe.Interpreter;
import net.kapitencraft.scripted.lang.exe.natives.NativeClass;
import net.kapitencraft.scripted.lang.exe.natives.Rename;

@NativeClass(pck = "scripted.lang")
public class System {

    @Rename("print")
    public static void println(Object in) {
        Interpreter.output.accept(Interpreter.stringify(in));
    }

    public static int time() {
        return (int) Interpreter.elapsedMillis();
    }

    public static int[] range(int min, int max) {
        int[] range = new int[max - min];
        for (int i = min; i < max; i++) {
            range[i - min] = i;
        }
        return range;
    }

    public static int[] range(int min, int max, int step) {
        int[] range = new int[(max - min) / step];
        for (int i = min; i < max; i+=step) {
            range[i - min] = i;
        }
        return range;
    }
}
