package net.kapitencraft.scripted.code.exe.methods.builder.method;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.util.Functions;

import java.util.function.Supplier;

public class MB9P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9> implements InstMapper<P1, R>, Returning<R> {
    private final VarType<R> retType;
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;
    private final ParamInst<P3> param3;
    private final ParamInst<P4> param4;
    private final ParamInst<P5> param5;
    private final ParamInst<P6> param6;
    private final ParamInst<P7> param7;
    private final ParamInst<P8> param8;
    private final ParamInst<P9> param9;
    private final DoubleMap<VarType<?>, String, MB10P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9, ?>> children = new DoubleMap<>();

    private Functions.F9<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> executor;

    public MB9P(VarType<R> retType, ParamInst<P1> param1, ParamInst<P2> param2, ParamInst<P3> param3, ParamInst<P4> param4, ParamInst<P5> param5, ParamInst<P6> param6, ParamInst<P7> param7, ParamInst<P8> param8, ParamInst<P9> param9) {
        this.retType = retType;
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.param4 = param4;
        this.param5 = param5;
        this.param6 = param6;
        this.param7 = param7;
        this.param8 = param8;
        this.param9 = param9;
    }

    public <P10> MB10P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> withParam(String name, Supplier<? extends VarType<P10>> type) {
        return (MB10P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>)  this.children.computeIfAbsent(type.get(), name, (type1, string) -> new MB10P<>(retType, param1, param2, param3, param4, param5, param6, param7, param8, param9, new ParamInst<>(type1, string)));
    }

    public MB9P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9> executes(Functions.F9<P1, P2, P3, P4, P5, P6, P7, P8, P9, R> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }

    public <P10> MB10P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> withParam(ParamInst<P10> inst) {
        return (MB10P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>) this.children.computeIfAbsent(inst.type(), inst.name(), (type1, string) -> new MB10P<>(retType, param1, param2, param3, param4, param5, param6, param7, param8, param9, inst));
    }
}
