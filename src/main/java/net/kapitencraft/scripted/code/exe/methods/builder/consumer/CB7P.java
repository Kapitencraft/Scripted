package net.kapitencraft.scripted.code.exe.methods.builder.consumer;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.kap_lib.stream.Consumers;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.exe.methods.builder.node.consumer.CN7P;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CB7P<P1, P2, P3, P4, P5, P6, P7> implements InstMapper<P1, Void> {
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;
    private final ParamInst<P3> param3;
    private final ParamInst<P4> param4;
    private final ParamInst<P5> param5;
    private final ParamInst<P6> param6;
    private final ParamInst<P7> param7;
    private final DoubleMap<VarType<?>, String, CB8P<P1, P2, P3, P4, P5, P6, P7, ?>> children = new DoubleMap<>();

    private Consumers.C7<P1, P2, P3, P4, P5, P6, P7> executor;

    private final Returning<Void> parent;

    public CB7P(ParamInst<P1> param1, ParamInst<P2> param2, ParamInst<P3> param3, ParamInst<P4> param4, ParamInst<P5> param5, ParamInst<P6> param6, ParamInst<P7> param7, Returning<Void> parent) {
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.param4 = param4;
        this.param5 = param5;
        this.param6 = param6;
        this.param7 = param7;
        this.parent = parent;
    }

    @Override
    public Returning<Void> getRootParent() {
        return parent;
    }

    @Override
    public void applyNodes(Consumer<ReturningNode<Void>> consumer) {
        if (this.executor != null) consumer.accept(new CN7P<>(param1, param2, param3, param4, param5, param6, param7, executor));
        if (!this.children.isEmpty()) this.children.actualValues().forEach(mb -> mb.applyNodes(consumer));
    }

    public <P8> CB8P<P1, P2, P3, P4, P5, P6, P7, P8> withParam(String name, Supplier<? extends VarType<P8>> type) {
        return withParam(ParamInst.create(name, type));
    }

    public CB7P<P1, P2, P3, P4, P5, P6, P7> executes(Consumers.C7<P1, P2, P3, P4, P5, P6, P7> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }

    public <P8> CB8P<P1, P2, P3, P4, P5, P6, P7, P8> withParam(ParamInst<P8> inst) {
        return (CB8P<P1, P2, P3, P4, P5, P6, P7, P8>) this.children.computeIfAbsent(inst.type(), inst.name(), (type1, string) -> new CB8P<>(param1, param2, param3, param4, param5, param6, param7, inst, parent));
    }

    public <P8, P9, P10> CB10P<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> params(
            ParamInst<P8> param8,
            ParamInst<P9> param9,
            ParamInst<P10> param10
    ) {
        return withParam(param8).params(param9, param10);
    }

    public <P8, P9> CB9P<P1, P2, P3, P4, P5, P6, P7, P8, P9> params(
            ParamInst<P8> param8,
            ParamInst<P9> param9
    ) {
        return withParam(param8).withParam(param9);
    }
}
