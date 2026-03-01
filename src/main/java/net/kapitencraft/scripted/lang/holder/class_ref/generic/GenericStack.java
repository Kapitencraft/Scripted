package net.kapitencraft.scripted.lang.holder.class_ref.generic;

import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GenericStack {
    private final List<Map<String, ClassReference>> stack = new ArrayList<>();

    public void push(Map<String, ClassReference> data) {
        stack.add(data);
    }

    public void pop() {
        if (stack.isEmpty())
            throw new IllegalStateException("generic stack underflow");
        stack.remove(stack.size()-1);
    }

    public Optional<ClassReference> getValue(String name) {
        int index = stack.size() - 1;
        while (index >= 0) {
            if (stack.get(index).containsKey(name)) return Optional.of(stack.get(index).get(name));
            index--;
        }
        return Optional.empty();
    }
}
