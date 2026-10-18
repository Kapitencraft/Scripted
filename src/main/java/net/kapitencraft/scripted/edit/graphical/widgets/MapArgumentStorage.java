package net.kapitencraft.scripted.edit.graphical.widgets;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MapArgumentStorage implements ArgumentStorage {
    private final Map<String, Entry> arguments;

    private MapArgumentStorage(Map<String, Entry> arguments) {
        this.arguments = arguments;
    }

    public static MapArgumentStorage createSingle(String name, Consumer<ExprCodeWidget> setter, Supplier<ExprCodeWidget> getter) {
        return new MapArgumentStorage(Map.of(name, new Entry(setter, getter)));
    }

    public static MapArgumentStorage createDouble(String name1, Consumer<ExprCodeWidget> setter1, Supplier<ExprCodeWidget> getter1,
                                               String name2, Consumer<ExprCodeWidget> setter2, Supplier<ExprCodeWidget> getter2
    ) {
        return new MapArgumentStorage(
                Map.of(name1, new Entry(setter1, getter1),
                        name2, new Entry(setter2, getter2)
                )
        );
    }

    public static MapArgumentStorage create(Map<String, ExprCodeWidget> args) {
        ImmutableMap.Builder<String, Entry> builder = new ImmutableMap.Builder<>();
        for (String s : args.keySet()) {
            builder.put(s, new Entry(w -> args.put(s, w), () -> args.get(s)));
        }
        return new MapArgumentStorage(builder.build());
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
