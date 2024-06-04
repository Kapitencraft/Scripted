package net.kapitencraft.scripted.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Leveled<K, V> {
    private final HashMap<K, V> content = new HashMap<>();
    private final List<Integer> levels = new ArrayList<>();

    public void push() {
        levels.add(content.size());
    }

    public void pop() {
        int index = levels.get(levels.size() - 1);
        List<Map.Entry<K, V>> set = new ArrayList<>(content.entrySet());
        content.clear();
        for (int i = 0; i < index; i++) {
            Map.Entry<K, V> entry = set.get(i);
            content.put(entry.getKey(), entry.getValue());
        }
    }

    public V getType(K name) {
        return content.get(name);
    }

    public void addValue(K key, V value) {
        content.put(key, value);
    }
}
