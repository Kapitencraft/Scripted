package net.kapitencraft.scripted.edit.graphical.widgets;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ArgumentStorage {
    private final Map<String, Entry> arguments;

    private ArgumentStorage(Map<String, Entry> arguments) {
        this.arguments = arguments;
    }

    public static ArgumentStorage createSingle(String name, Consumer<ExprCodeWidget> setter, Supplier<ExprCodeWidget> getter) {
        return new ArgumentStorage(Map.of(name, new Entry(setter, getter)));
    }

    public static ArgumentStorage create(Map<String, ExprCodeWidget> args) {
        ImmutableMap.Builder<String, Entry> builder = new ImmutableMap.Builder<>();
        for (String s : args.keySet()) {
            builder.put(s, new Entry(w -> args.put(s, w), () -> args.get(s)));
        }
        return new ArgumentStorage(builder.build());
    }

    private record Entry(Consumer<ExprCodeWidget> setter, Supplier<ExprCodeWidget> getter) {
    }

    public void remove(String name) {
        arguments.get(name).setter.accept(null);
    }

    public ExprCodeWidget get(String name) {
        return arguments.get(name).getter.get();
    }
}
