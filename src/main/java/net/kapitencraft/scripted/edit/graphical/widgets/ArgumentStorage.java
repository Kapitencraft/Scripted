package net.kapitencraft.scripted.edit.graphical.widgets;

import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ArgumentStorage {
    private final Entry[] arguments;

    private ArgumentStorage(Entry[] arguments) {
        this.arguments = arguments;
    }

    public static ArgumentStorage createSingle(Consumer<ExprCodeWidget> setter, Supplier<ExprCodeWidget> getter) {
        return new ArgumentStorage(new Entry[]{new Entry(setter, getter)});
    }

    public static ArgumentStorage create(List<ExprCodeWidget> args) {
        List<Entry> list = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            final int j = i;
            list.add(new Entry(w -> args.set(j, w), () -> args.get(j)));
        }
        return new ArgumentStorage(list.toArray(Entry[]::new));
    }

    private record Entry(Consumer<ExprCodeWidget> setter, Supplier<ExprCodeWidget> getter) {
    }

    public void remove(int idx) {
        arguments[idx].setter.accept(null);
    }

    public ExprCodeWidget get(int idx) {
        return arguments[idx].getter.get();
    }
}
