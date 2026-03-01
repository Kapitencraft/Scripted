package net.kapitencraft.scripted.lang.run;

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
        return object == null ? "null" : object.toString();
    }

    public static long elapsedMillis() {
        return System.currentTimeMillis() - millisAtStart;
    }
}
