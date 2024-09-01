package net.kapitencraft.scripted.code.exe.methods.builder.consumer;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.kap_lib.stream.Consumers;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.exe.methods.builder.node.consumer.CN6P;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CB6P<P1, P2, P3, P4, P5, P6> implements InstMapper<P1, Void> {
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;
    private final ParamInst<P3> param3;
    private final ParamInst<P4> param4;
    private final ParamInst<P5> param5;
    private final ParamInst<P6> param6;
    private final DoubleMap<VarType<?>, String, CB7P<P1, P2, P3, P4, P5, P6, ?>> children = new DoubleMap<>();

    private Consumers.C6<P1, P2, P3, P4, P5, P6> executor;

    private final Returning<Void> parent;

    public CB6P(ParamInst<P1> param1, ParamInst<P2> param2, ParamInst<P3> param3, ParamInst<P4> param4, ParamInst<P5> param5, ParamInst<P6> param6, Returning<Void> parent) {
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.param4 = param4;
        this.param5 = param5;
        this.param6 = param6;
        this.parent = parent;
    }

    @Override
    public Returning<Void> getRootParent() {
        return parent;
    }

    @Override
    public void applyNodes(Consumer<ReturningNode<Void>> consumer) {
        if (this.executor != null) consumer.accept(new CN6P<>(param1, param2, param3, param4, param5, param6, executor));
        if (!this.children.isEmpty()) this.children.actualValues().forEach(mb -> mb.applyNodes(consumer));
    }

    public <P7> CB7P<P1, P2, P3, P4, P5, P6, P7> withParam(String name, Supplier<? extends VarType<P7>> type) {
        return withParam(ParamInst.create(name, type));
    }

    public CB6P<P1, P2, P3, P4, P5, P6> executes(Consumers.C6<P1, P2, P3, P4, P5, P6> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }

    public <P7> CB7P<P1, P2, P3, P4, P5, P6, P7> withParam(ParamInst<P7> inst) {
        return (CB7P<P1, P2, P3, P4, P5, P6, P7>) this.children.computeIfAbsent(inst.type(), inst.name(), (type1, string) -> new CB7P<>(param1, param2, param3, param4, param5, param6, inst, parent));
    }

    public <P7, P8, P9, P10> CB10P<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> params(
            ParamInst<P7> param7,
            ParamInst<P8> param8,
            ParamInst<P9> param9,
            ParamInst<P10> param10
    ) {
        return withParam(param7).params(param8, param9, param10);
    }

    public <P7, P8, P9> CB9P<P1, P2, P3, P4, P5, P6, P7, P8, P9> params(
            ParamInst<P7> param7,
            ParamInst<P8> param8,
            ParamInst<P9> param9
    ) {
        return withParam(param7).params(param8, param9);
    }

    public <P7, P8> CB8P<P1, P2, P3, P4, P5, P6, P7, P8> params(
            ParamInst<P7> param7,
            ParamInst<P8> param8
    ) {
        return withParam(param7).withParam(param8);
    }
}
