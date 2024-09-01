package net.kapitencraft.scripted.code.exe.methods.builder.method;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.exe.methods.builder.node.method.MethodNode;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MethodBuilder<R> implements Returning<R> {
    protected final VarType<R> retType;
    private Supplier<R> executor;
    private final DoubleMap<VarType<?>, String, MB1P<R, ?>> children = new DoubleMap<>();

    public MethodBuilder(VarType<R> retType) {
        this.retType = retType;
    }

    public <P1> MB1P<R, P1> withParam(String name, Supplier<? extends VarType<P1>> type) {
        return (MB1P<R, P1>) this.children.computeIfAbsent(type.get(), name, (type1, string) -> new MB1P<>(retType, new ParamInst<>(type1, string), this));
    }

    public MethodBuilder<R> executes(Supplier<R> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }


    public boolean assertExecutorExisting() {
        return executor != null;
    }

    @Override
    public void applyNodes(Consumer<ReturningNode<R>> consumer) {
        if (this.executor != null) consumer.accept(new MethodNode<>(retType, executor));
        if (!this.children.isEmpty()) this.children.actualValues().forEach(mb -> mb.applyNodes(consumer));
    }
}
