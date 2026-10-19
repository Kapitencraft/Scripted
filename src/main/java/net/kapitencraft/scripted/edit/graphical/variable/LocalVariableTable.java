package net.kapitencraft.scripted.edit.graphical.variable;

import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LocalVariableTable implements Iterable<LocalVariableTable.Entry> {
    private final Entry[] entries = new Entry[256];
    private int localCount;
    private int scopeDepth = 0;

    public LocalVariableTable() {
    }

    public void push() {
        this.scopeDepth++;
    }

    public void pop() {
        int i = localCount - 1;
        while (i >= 0 && entries[i].depth >= scopeDepth) {
            i--;
        }
        localCount = (byte) (i + 1);
        scopeDepth--;
    }

    /**
     * @param name     the name of the LV
     * @param category the category of the LV
     * @return true when the LV was added, false if a LV with that name already existed
     */
    public boolean add(String name, ExprCategory category) {
        if (get(name) != null) {
            return false;
        }
        entries[localCount] = new Entry(name, category, scopeDepth);
        return true;
    }

    public @Nullable Entry get(String name) {
        for (int i = localCount - 1; i >= 0; i--) {
            Entry entry = entries[i];
            if (Objects.equals(entry.name, name))
                return entry;
        }
        return null;
    }

    public boolean has(@Nullable String selected) {
        return get(selected) != null;
    }

    public ExprCategory getType(@Nullable String name) {
        Entry entry = get(name);
        return entry != null ? entry.category : ExprCategory.OTHER;
    }

    @Override
    public @NotNull Iterator<Entry> iterator() {
        return new TableIterator();
    }

    public int size() {
        return this.localCount;
    }

    public Entry get(int selectedIndex) {
        return this.entries[selectedIndex];
    }

    private class TableIterator implements Iterator<Entry> {
        int next = 0;

        @Override
        public boolean hasNext() {
            return next < localCount;
        }

        @Override
        public Entry next() {
            return entries[next++];
        }
    }

    public record Entry(String name, ExprCategory category, int depth) {
    }
}
