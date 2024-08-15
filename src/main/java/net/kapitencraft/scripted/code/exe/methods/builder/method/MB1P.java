package net.kapitencraft.scripted.code.exe.methods.builder.method;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.function.Function;
import java.util.function.Supplier;

public class MB1P<R, P1> implements InstMapper<P1, R>, Returning<R> {
    private final VarType<R> retType;
    private final ParamInst<P1> param1;
    private final DoubleMap<VarType<?>, String, MB2P<R, P1, ?>> children = new DoubleMap<>();

    private Function<P1, R> executor;

    public MB1P(VarType<R> retType, ParamInst<P1> param1) {
        this.retType = retType;
        this.param1 = param1;
    }


    public MB1P<R, P1> executes(Function<P1, R> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }

    public <P2> MB2P<R, P1, P2> withParam(String name, Supplier<? extends VarType<P2>> type) {
        return withParam(ParamInst.create(name, type));
    }

    public <P2> MB2P<R, P1, P2> withParam(ParamInst<P2> inst) {
        return (MB2P<R, P1, P2>) this.children.computeIfAbsent(inst.type(), inst.name(), (type1, string) -> new MB2P<>(retType, param1, inst));
    }

    public <P2, P3, P4, P5, P6, P7, P8, P9, P10> MB10P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> params(
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

    public <P2, P3, P4, P5, P6, P7, P8, P9> MB9P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9> params(
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

    public <P2, P3, P4, P5, P6, P7, P8> MB8P<R, P1, P2, P3, P4, P5, P6, P7, P8> params(
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

    public <P2, P3, P4, P5, P6, P7> MB7P<R, P1, P2, P3, P4, P5, P6, P7> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7
    ) {
        return withParam(param2).params(param3, param4, param5, param6, param7);
    }

    public <P2, P3, P4, P5, P6> MB6P<R, P1, P2, P3, P4, P5, P6> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6
    ) {
        return withParam(param2).params(param3, param4, param5, param6);
    }

    public <P2, P3, P4, P5> MB5P<R, P1, P2, P3, P4, P5> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3,
            ParamInst<P4> param4,
            ParamInst<P5> param5
    ) {
        return withParam(param2).params(param3, param4, param5);
    }

    public <P2, P3, P4> MB4P<R, P1, P2, P3, P4> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3,
            ParamInst<P4> param4
    ) {
        return withParam(param2).params(param3, param4);
    }

    public <P2, P3> MB3P<R, P1, P2, P3> params(
            ParamInst<P2> param2,
            ParamInst<P3> param3
    ) {
        return withParam(param2).withParam(param3);
    }
}
