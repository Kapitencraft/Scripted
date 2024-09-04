package net.kapitencraft.scripted.util;

import net.minecraft.Util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Leveled<K, V> {
    private final Deque<Map<K, V>> stack = Util.make(new ArrayDeque<>(), maps -> maps.add(new HashMap<>()));

    /**
     * push the stack; use pop to revert changes made after push
     */
    public void push() {
        stack.push(new HashMap<>(stack.getLast()));
    }

    /**
     * pop the stack; removes any changes made since the last `'push' call
     */
    public void pop() {
        stack.removeLast();
        if (stack.isEmpty()) throw new IllegalStateException("leveled has been completely cleared");
    }

    public V getValue(K name) {
        return stack.getLast().get(name);
    }

    public void addValue(K key, V value) {
        stack.getLast().put(key, value);
    }
}