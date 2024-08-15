package net.kapitencraft.scripted.code.exe.methods.builder.consumer;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.util.Consumers;

import java.util.function.Supplier;

public class CB5P<P1, P2, P3, P4, P5> implements InstMapper<P1, Void> {
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;
    private final ParamInst<P3> param3;
    private final ParamInst<P4> param4;
    private final ParamInst<P5> param5;
    private final DoubleMap<VarType<?>, String, CB6P<P1, P2, P3, P4, P5, ?>> children = new DoubleMap<>();

    private Consumers.C5<P1, P2, P3, P4, P5> executor;

    public CB5P(ParamInst<P1> param1, ParamInst<P2> param2, ParamInst<P3> param3, ParamInst<P4> param4, ParamInst<P5> param5) {
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.param4 = param4;
        this.param5 = param5;
    }

    public <P6> CB6P<P1, P2, P3, P4, P5, P6> withParam(String name, Supplier<? extends VarType<P6>> type) {
        return withParam(ParamInst.create(name, type));
    }

    public CB5P<P1, P2, P3, P4, P5> executes(Consumers.C5<P1, P2, P3, P4, P5> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }

    public <P6> CB6P<P1, P2, P3, P4, P5, P6> withParam(ParamInst<P6> inst) {
        return (CB6P<P1, P2, P3, P4, P5, P6>) this.children.computeIfAbsent(inst.type(), inst.name(), (type1, string) -> new CB6P<>(param1, param2, param3, param4, param5, inst));
    }

    public <P6, P7, P8, P9, P10> CB10P<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> params(
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8,
            ParamInst<P9> param9,
            ParamInst<P10> param10
    ) {
        return withParam(param6).params(param7, param8, param9, param10);
    }

    public <P6, P7, P8, P9> CB9P<P1, P2, P3, P4, P5, P6, P7, P8, P9> params(
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8,
            ParamInst<P9> param9
    ) {
        return withParam(param6).params(param7, param8, param9);
    }

    public <P6, P7, P8> CB8P<P1, P2, P3, P4, P5, P6, P7, P8> params(
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8
    ) {
        return withParam(param6).params(param7, param8);
    }

    public <P6, P7> CB7P<P1, P2, P3, P4, P5, P6, P7> params(
            ParamInst<P6> param6,
            ParamInst<P7> param7
    ) {
        return withParam(param6).withParam(param7);
    }
}
