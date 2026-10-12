package net.kapitencraft.scripted.lang.exe;

import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Consumer;

//will use VirtualMachine instead
public class Interpreter {

    public static Consumer<String> output = System.out::println;

    public static final Scanner in = new Scanner(System.in);

    public static boolean suppressClassLoad = false;

    public static long millisAtStart;

    public static void start() {
        millisAtStart = System.currentTimeMillis();
    }

    public static String stringify(Object object) {
        return switch (object) {
            case null -> "null";
            case int[] iA -> Arrays.toString(iA);
            case double[] dA -> Arrays.toString(dA);
            case float[] fA -> Arrays.toString(fA);
            case boolean[] bA -> Arrays.toString(bA);
            case char[] cA -> Arrays.toString(cA);
            case Object[] oA -> Arrays.toString(oA);
            default -> object.toString();
        };
    }

    public static long elapsedMillis() {
        return System.currentTimeMillis() - millisAtStart;
    }
}
