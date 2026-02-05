package net.kapitencraft.scripted.edit.graphical.variable;

import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LocalVariableTable {
    private final List<Map<String, ExprCategory>> stack = new ArrayList<>();

    public void push() {
        this.stack.addLast(new HashMap<>(this.stack.getLast()));
    }

    public void pop() {
        this.stack.removeLast();
    }

    @ApiStatus.Internal
    public Map<String, ExprCategory> active() {
        return stack.getLast();
    }

    public Set<String> getAll() {
        return active().keySet();
    }

    /**
     * @param name the name of the LV
     * @param category the category of the LV
     * @return true when the LV was added, false if a LV with that name already existed
     */
    public boolean add(String name, ExprCategory category) {
        if (active().containsKey(name))
            return false;
        active().put(name, category);
        return true;
    }

    public boolean has(@Nullable String selected) {
        return active().containsKey(selected);
    }

    public ExprCategory getType(@Nullable String name) {
        return active().getOrDefault(name, ExprCategory.OTHER);
    }
}
