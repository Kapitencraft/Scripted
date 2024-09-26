package net.kapitencraft.scripted.tool;

import java.util.HashMap;
import java.util.Map;

public class Util {

    public static <K, V> Map<K, V> mergeMaps(Map<K, V> base, Map<K, V> extension) {
        Map<K, V> temp = new HashMap<>(base);
        temp.putAll(extension);
        return Map.copyOf(temp);
    }

    //that's because Objects.requireNonNullElse checks else for null
    public static <K> K nonNullElse(K main, K other) {
        return main != null ? main : other;
    }
}
