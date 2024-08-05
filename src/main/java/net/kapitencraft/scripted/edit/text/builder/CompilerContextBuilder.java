package net.kapitencraft.scripted.edit.text.builder;

import java.util.ArrayList;
import java.util.List;

public class CompilerContextBuilder<S> {
    private final List<?> parameters = new ArrayList<>();
    private final S source;

    public CompilerContextBuilder(S source) {
        this.source = source;
    }

    public <T> void add(T value) {
    }

    public S getSource() {
        return source;
    }

    public <T> T getValue(int index) {
        return (T) parameters.get(index);
    }
}
