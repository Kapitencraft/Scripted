package net.kapitencraft.scripted.edit.text.language.java;

import net.kapitencraft.scripted.edit.text.builder.CompilerBuilder;
import net.kapitencraft.scripted.edit.text.node.CompilerNode;

public class JavaCompilerBuilder<T> extends CompilerBuilder<T, JavaCompilerBuilder<T>> {
    @Override
    protected JavaCompilerBuilder<T> getThis() {
        return this;
    }

    @Override
    public CompilerNode<T> build() {
        return null;
    }
}
