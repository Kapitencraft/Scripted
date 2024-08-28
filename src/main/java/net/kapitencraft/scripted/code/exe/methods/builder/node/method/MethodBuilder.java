package net.kapitencraft.scripted.code.exe.methods.builder.node.method;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.function.Supplier;

public class MethodBuilder<R> implements Returning<R> {
    protected final VarType<R> retType;
    private Supplier<R> executor;
    private final DoubleMap<VarType<?>, String, MN1P<R, ?>> children = new DoubleMap<>();

    public MethodBuilder(VarType<R> retType) {
        this.retType = retType;
    }

    public <P1> MN1P<R, P1> withParam(String name, Supplier<? extends VarType<P1>> type) {
        return (MN1P<R, P1>) this.children.computeIfAbsent(type.get(), name, (type1, string) -> new MN1P<>(retType, new ParamInst<>(type1, string)));
    }

    public MethodBuilder<R> executes(Supplier<R> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }


    public boolean assertExecutorExisting() {
        return executor != null;
    }
}
