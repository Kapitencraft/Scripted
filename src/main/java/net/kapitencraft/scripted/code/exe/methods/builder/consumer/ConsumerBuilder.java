package net.kapitencraft.scripted.code.exe.methods.builder.consumer;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.function.Supplier;

public class ConsumerBuilder {
    private Runnable executor;
    private final DoubleMap<VarType<?>, String, CB1P<?>> children = new DoubleMap<>();

    public <P1> CB1P<P1> withParam(String name, Supplier<? extends VarType<P1>> type) {
        return (CB1P<P1>) this.children.computeIfAbsent(type.get(), name, (type1, string) -> new CB1P<>(new ParamInst<>(type1, string)));
    }

    public ConsumerBuilder executes(Runnable executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }


    public boolean assertExecutorExisting() {
        return executor != null;
    }
}
