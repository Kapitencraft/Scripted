package net.kapitencraft.scripted.code.exe.methods.builder.consumer;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.kap_lib.stream.Consumers;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.exe.methods.builder.node.consumer.CN8P;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CB8P<P1, P2, P3, P4, P5, P6, P7, P8> implements InstMapper<P1, Void> {
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;
    private final ParamInst<P3> param3;
    private final ParamInst<P4> param4;
    private final ParamInst<P5> param5;
    private final ParamInst<P6> param6;
    private final ParamInst<P7> param7;
    private final ParamInst<P8> param8;
    private final DoubleMap<VarType<?>, String, CB9P<P1, P2, P3, P4, P5, P6, P7, P8, ?>> children = new DoubleMap<>();

    private Consumers.C8<P1, P2, P3, P4, P5, P6, P7, P8> executor;

    private final Returning<Void> parent;

    public CB8P(ParamInst<P1> param1, ParamInst<P2> param2, ParamInst<P3> param3, ParamInst<P4> param4, ParamInst<P5> param5, ParamInst<P6> param6, ParamInst<P7> param7, ParamInst<P8> param8, Returning<Void> parent) {
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.param4 = param4;
        this.param5 = param5;
        this.param6 = param6;
        this.param7 = param7;
        this.param8 = param8;
        this.parent = parent;
    }

    @Override
    public Returning<Void> getRootParent() {
        return parent;
    }

    @Override
    public void applyNodes(Consumer<ReturningNode<Void>> consumer) {
        if (this.executor != null) consumer.accept(new CN8P<>(param1, param2, param3, param4, param5, param6, param7, param8, executor));
        if (!this.children.isEmpty()) this.children.actualValues().forEach(mb -> mb.applyNodes(consumer));
    }

    public <P9> CB9P<P1, P2, P3, P4, P5, P6, P7, P8, P9> withParam(String name, Supplier<? extends VarType<P9>> type) {
        return withParam(ParamInst.create(name, type));
    }

    public CB8P<P1, P2, P3, P4, P5, P6, P7, P8> executes(Consumers.C8<P1, P2, P3, P4, P5, P6, P7, P8> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }



    public <P9> CB9P<P1, P2, P3, P4, P5, P6, P7, P8, P9> withParam(ParamInst<P9> inst) {
        return (CB9P<P1, P2, P3, P4, P5, P6, P7, P8, P9>) this.children.computeIfAbsent(inst.type(), inst.name(), (type1, string) -> new CB9P<>(param1, param2, param3, param4, param5, param6, param7, param8, inst, parent));
    }

    public <P9, P10> CB10P<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> params(
            ParamInst<P9> param9,
            ParamInst<P10> param10
    ) {
        return withParam(param9).withParam(param10);
    }
}
