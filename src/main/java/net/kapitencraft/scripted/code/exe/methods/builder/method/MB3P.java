package net.kapitencraft.scripted.code.exe.methods.builder.method;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.util.Functions;

import java.util.function.Supplier;

public class MB3P<R, P1, P2, P3> implements InstMapper<P1, R>, Returning<R> {
    private final VarType<R> retType;
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;
    private final ParamInst<P3> param3;
    private final DoubleMap<VarType<?>, String, MB4P<R, P1, P2, P3, ?>> children = new DoubleMap<>();

    private Functions.F3<P1, P2, P3, R> executor;

    public MB3P(VarType<R> retType, ParamInst<P1> param1, ParamInst<P2> param2, ParamInst<P3> param3) {
        this.retType = retType;
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
    }

    public <P4> MB4P<R, P1, P2, P3, P4> withParam(String name, Supplier<? extends VarType<P4>> type) {
        return withParam(ParamInst.create(name, type));
    }

    public MB3P<R, P1, P2, P3> executes(Functions.F3<P1, P2, P3, R> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }

    public <P4> MB4P<R, P1, P2, P3, P4> withParam(ParamInst<P4> inst) {
        return (MB4P<R, P1, P2, P3, P4>) this.children.computeIfAbsent(inst.type(), inst.name(), (type1, string) -> new MB4P<>(retType, param1, param2, param3, inst));
    }

    public <P4, P5, P6, P7, P8, P9, P10> MB10P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> params(
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8,
            ParamInst<P9> param9,
            ParamInst<P10> param10
    ) {
        return withParam(param4).params(param5, param6, param7, param8, param9, param10);
    }

    public <P4, P5, P6, P7, P8, P9> MB9P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9> params(
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8,
            ParamInst<P9> param9
    ) {
        return withParam(param4).params(param5, param6, param7, param8, param9);
    }

    public <P4, P5, P6, P7, P8> MB8P<R, P1, P2, P3, P4, P5, P6, P7, P8> params(
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7,
            ParamInst<P8> param8
    ) {
        return withParam(param4).params(param5, param6, param7, param8);
    }

    public <P4, P5, P6, P7> MB7P<R, P1, P2, P3, P4, P5, P6, P7> params(
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6,
            ParamInst<P7> param7
    ) {
        return withParam(param4).params(param5, param6, param7);
    }

    public <P4, P5, P6> MB6P<R, P1, P2, P3, P4, P5, P6> params(
            ParamInst<P4> param4,
            ParamInst<P5> param5,
            ParamInst<P6> param6
    ) {
        return withParam(param4).params(param5, param6);
    }

    public <P4, P5> MB5P<R, P1, P2, P3, P4, P5> params(
            ParamInst<P4> param4,
            ParamInst<P5> param5
    ) {
        return withParam(param4).withParam(param5);
    }
}
