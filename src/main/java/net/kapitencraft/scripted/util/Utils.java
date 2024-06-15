package net.kapitencraft.scripted.util;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.Objects;

public class Utils {

    @Contract("null -> true")
    public static boolean checkAnyNull(Object... objects) {
        return Arrays.stream(objects).anyMatch(Objects::isNull);
    }
}
