package net.kapitencraft.scripted.code.exe.methods.builder.consumer;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.exe.methods.builder.node.consumer.ConsumerNode;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConsumerBuilder implements Returning<Void> {
    private Runnable executor;
    private final DoubleMap<VarType<?>, String, CB1P<?>> children = new DoubleMap<>();

    public <P1> CB1P<P1> withParam(String name, Supplier<? extends VarType<P1>> type) {
        return (CB1P<P1>) this.children.computeIfAbsent(type.get(), name, (type1, string) -> new CB1P<>(new ParamInst<>(type1, string), this));
    }

    public ConsumerBuilder executes(Runnable executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }


    public boolean assertExecutorExisting() {
        return executor != null;
    }

    @Override
    public void applyNodes(Consumer<ReturningNode<Void>> consumer) {
        if (this.executor != null) consumer.accept(new ConsumerNode(executor));
        if (!this.children.isEmpty()) this.children.actualValues().forEach(mb -> mb.applyNodes(consumer));
    }
}
