package net.kapitencraft.scripted.code.exe.methods.builder;

import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;

import java.util.function.Consumer;

public interface Returning<R> {

    void applyNodes(Consumer<ReturningNode<R>> consumer);
}
