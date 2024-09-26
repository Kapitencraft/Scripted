package net.kapitencraft.scripted.lang.env.abst;

import java.util.HashMap;
import java.util.Map;

public class Leveled<K, V> extends DequeStack<Map<K, V>> {

    protected Leveled() {
        super(new HashMap<>(), HashMap::new);
    }

    protected V getValue(K name) {
        return getLast().get(name);
    }

    protected void addValue(K key, V value) {
        getLast().put(key, value);
    }

    public boolean has(K key) {
        return this.getLast().containsKey(key);
    }
}