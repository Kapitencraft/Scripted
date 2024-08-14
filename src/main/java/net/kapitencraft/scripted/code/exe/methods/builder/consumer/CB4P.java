package net.kapitencraft.scripted.code.exe.methods.builder.consumer;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.util.Consumers;

import java.util.function.Supplier;

public class CB4P<P1, P2, P3, P4> {
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;
    private final ParamInst<P3> param3;
    private final ParamInst<P4> param4;
    private final DoubleMap<VarType<?>, String, CB5P<P1, P2, P3, P4, ?>> children = new DoubleMap<>();

    private Consumers.C4<P1, P2, P3, P4> executor;

    public CB4P(ParamInst<P1> param1, ParamInst<P2> param2, ParamInst<P3> param3, ParamInst<P4> param4) {
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.param4 = param4;
    }

    public <P5> CB5P<P1, P2, P3, P4, P5> withParam(String name, Supplier<? extends VarType<P5>> type) {
        return withParam(ParamInst.create(name, type));
    }

    public CB4P<P1, P2, P3, P4> executes(Consumers.C4<P1, P2, P3, P4> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }

    public <P5> CB5P<P1, P2, P3, P4, P5> withParam(ParamInst<P5> inst) {
        return (CB5P<P1, P2, P3, P4, P5>) this.children.computeIfAbsent(inst.type(), inst.name(), (type1, string) -> new CB5P<>(param1, param2, param3, param4, inst));
    }

    public <P5, P6, P7, P8, P9, P10> CB10P<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> params(
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8,
            ParamInst<P9> param9,
            ParamInst<P10> param10
    ) {
        return withParam(param5).params(param6, param7, param8, param9, param10);
    }

    public <P5, P6, P7, P8, P9> CB9P<P1, P2, P3, P4, P5, P6, P7, P8, P9> params(
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8,
            ParamInst<P9> param9
    ) {
        return withParam(param5).params(param6, param7, param8, param9);
    }

    public <P5, P6, P7, P8> CB8P<P1, P2, P3, P4, P5, P6, P7, P8> params(
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8
    ) {
        return withParam(param5).params(param6, param7, param8);
    }

    public <P5, P6, P7> CB7P<P1, P2, P3, P4, P5, P6, P7> params(
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7
    ) {
        return withParam(param5).params(param6, param7);
    }

    public <P5, P6> CB6P<P1, P2, P3, P4, P5, P6> params(
            ParamInst<P5> param5,
            ParamInst<P6> param6
    ) {
        return withParam(param5).withParam(param6);
    }
}
