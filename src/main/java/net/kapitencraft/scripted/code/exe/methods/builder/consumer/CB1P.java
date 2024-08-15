package net.kapitencraft.scripted.code.exe.methods.builder.consumer;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CB1P<P1> implements InstMapper<P1, Void> {
    private final ParamInst<P1> param1;
    private final DoubleMap<VarType<?>, String, CB2P<P1, ?>> children = new DoubleMap<>();

    private Consumer<P1> executor;

    public CB1P(ParamInst<P1> param1) {
        this.param1 = param1;
    }


    public CB1P<P1> executes(Consumer<P1> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }

    public <P2> CB2P<P1, P2> withParam(String name, Supplier<? extends VarType<P2>> type) {
        return withParam(ParamInst.create(name, type));
    }

    public <P2> CB2P<P1, P2> withParam(ParamInst<P2> inst) {
        return (CB2P<P1, P2>) this.children.computeIfAbsent(inst.type(), inst.name(), (type1, string) -> new CB2P<>(param1, inst));
    }

    public <P2, P3, P4, P5, P6, P7, P8, P9, P10> CB10P<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8,
            ParamInst<P9> param9,
            ParamInst<P10> param10
    ) {
        return withParam(param2).params(param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public <P2, P3, P4, P5, P6, P7, P8, P9> CB9P<P1, P2, P3, P4, P5, P6, P7, P8, P9> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8,
            ParamInst<P9> param9
    ) {
        return withParam(param2).params(param3, param4, param5, param6, param7, param8, param9);
    }

    public <P2, P3, P4, P5, P6, P7, P8> CB8P<P1, P2, P3, P4, P5, P6, P7, P8> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8
    ) {
        return withParam(param2).params(param3, param4, param5, param6, param7, param8);
    }

    public <P2, P3, P4, P5, P6, P7> CB7P<P1, P2, P3, P4, P5, P6, P7> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7
    ) {
        return withParam(param2).params(param3, param4, param5, param6, param7);
    }

    public <P2, P3, P4, P5, P6> CB6P<P1, P2, P3, P4, P5, P6> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6
    ) {
        return withParam(param2).params(param3, param4, param5, param6);
    }

    public <P2, P3, P4, P5> CB5P<P1, P2, P3, P4, P5> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5
    ) {
        return withParam(param2).params(param3, param4, param5);
    }

    public <P2, P3, P4> CB4P<P1, P2, P3, P4> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3,
            ParamInst<P4> param4
    ) {
        return withParam(param2).params(param3, param4);
    }

    public <P2, P3> CB3P<P1, P2, P3> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3
    ) {
        return withParam(param2).withParam(param3);
    }
}
