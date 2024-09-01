package net.kapitencraft.scripted.code.exe.methods.builder.consumer;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.exe.methods.builder.node.consumer.CN2P;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CB2P<P1, P2> implements InstMapper<P1, Void> {
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;
    private final DoubleMap<VarType<?>, String, CB3P<P1, P2, ?>> children = new DoubleMap<>();

    private BiConsumer<P1, P2> executor;

    private final Returning<Void> parent;

    public CB2P(ParamInst<P1> param1, ParamInst<P2> param2, Returning<Void> parent) {
        this.param1 = param1;
        this.param2 = param2;
        this.parent = parent;
    }

    @Override
    public Returning<Void> getRootParent() {
        return parent;
    }

    @Override
    public void applyNodes(Consumer<ReturningNode<Void>> consumer) {
        if (this.executor != null) consumer.accept(new CN2P<>(param1, param2, executor));
        if (!this.children.isEmpty()) this.children.actualValues().forEach(mb -> mb.applyNodes(consumer));

    }

    public <P3> CB3P<P1, P2, P3> withParam(String name, Supplier<? extends VarType<P3>> type) {
        return withParam(ParamInst.create(name, type));
    }

    public CB2P<P1, P2> executes(BiConsumer<P1, P2> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }

    public <P3> CB3P<P1, P2, P3> withParam(ParamInst<P3> inst) {
        return (CB3P<P1, P2, P3>) this.children.computeIfAbsent(inst.type(), inst.name(), (type1, string) -> new CB3P<>(param1, param2, inst, parent));
    }

    public <P3, P4, P5, P6, P7, P8, P9, P10> CB10P<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> params(
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8,
            ParamInst<P9> param9,
            ParamInst<P10> param10
    ) {
        return withParam(param3).params(param4, param5, param6, param7, param8, param9, param10);
    }

    public <P3, P4, P5, P6, P7, P8, P9> CB9P<P1, P2, P3, P4, P5, P6, P7, P8, P9> params(
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8,
            ParamInst<P9> param9
    ) {
        return withParam(param3).params(param4, param5, param6, param7, param8, param9);
    }

    public <P3, P4, P5, P6, P7, P8> CB8P<P1, P2, P3, P4, P5, P6, P7, P8> params(
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8
    ) {
        return withParam(param3).params(param4, param5, param6, param7, param8);
    }

    public <P3, P4, P5, P6, P7> CB7P<P1, P2, P3, P4, P5, P6, P7> params(
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7
    ) {
        return withParam(param3).params(param4, param5, param6, param7);
    }

    public <P3, P4, P5, P6> CB6P<P1, P2, P3, P4, P5, P6> params(
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6
    ) {
        return withParam(param3).params(param4, param5, param6);
    }

    public <P3, P4, P5> CB5P<P1, P2, P3, P4, P5> params(
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5
    ) {
        return withParam(param3).params(param4, param5);
    }

    public <P3, P4> CB4P<P1, P2, P3, P4> params(
            ParamInst<P3> param3,
            ParamInst<P4> param4
    ) {
        return withParam(param3).withParam(param4);
    }
}
