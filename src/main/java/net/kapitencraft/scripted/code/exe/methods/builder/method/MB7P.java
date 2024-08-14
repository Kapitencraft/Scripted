package net.kapitencraft.scripted.code.exe.methods.builder.method;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.ReturningInst;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.util.Functions;

import java.util.function.Supplier;

public class MB7P<R, P1, P2, P3, P4, P5, P6, P7> implements ReturningInst<P1, R> {
    private final VarType<R> retType;
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;
    private final ParamInst<P3> param3;
    private final ParamInst<P4> param4;
    private final ParamInst<P5> param5;
    private final ParamInst<P6> param6;
    private final ParamInst<P7> param7;
    private final DoubleMap<VarType<?>, String, MB8P<R, P1, P2, P3, P4, P5, P6, P7, ?>> children = new DoubleMap<>();

    private Functions.F7<P1, P2, P3, P4, P5, P6, P7, R> executor;

    public MB7P(VarType<R> retType, ParamInst<P1> param1, ParamInst<P2> param2, ParamInst<P3> param3, ParamInst<P4> param4, ParamInst<P5> param5, ParamInst<P6> param6, ParamInst<P7> param7) {
        this.retType = retType;
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.param4 = param4;
        this.param5 = param5;
        this.param6 = param6;
        this.param7 = param7;
    }

    public <P8> MB8P<R, P1, P2, P3, P4, P5, P6, P7, P8> withParam(String name, Supplier<? extends VarType<P8>> type) {
        return withParam(ParamInst.create(name, type));
    }

    public MB7P<R, P1, P2, P3, P4, P5, P6, P7> executes(Functions.F7<P1, P2, P3, P4, P5, P6, P7, R> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }

    public <P8> MB8P<R, P1, P2, P3, P4, P5, P6, P7, P8> withParam(ParamInst<P8> inst) {
        return (MB8P<R, P1, P2, P3, P4, P5, P6, P7, P8>) this.children.computeIfAbsent(inst.type(), inst.name(), (type1, string) -> new MB8P<>(retType, param1, param2, param3, param4, param5, param6, param7, inst));
    }

    public <P8, P9, P10> MB10P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> params(
            ParamInst<P8> param8,
            ParamInst<P9> param9,
            ParamInst<P10> param10
    ) {
        return withParam(param8).params(param9, param10);
    }

    public <P8, P9> MB9P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9> params(
            ParamInst<P8> param8,
            ParamInst<P9> param9
    ) {
        return withParam(param8).withParam(param9);
    }
}
