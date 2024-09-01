package net.kapitencraft.scripted.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Leveled<K, V> {
    private final Deque<Map<K, V>> stack = new ArrayDeque<>();

    public void push() {
        stack.push(new HashMap<>(stack.getLast()));
    }

    public void pop() {
        stack.removeLast();
    }

    public V getValue(K name) {
        return stack.getLast().get(name);
    }

    public void addValue(K key, V value) {
        stack.getLast().put(key, value);
    }
}