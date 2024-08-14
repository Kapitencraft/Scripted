package net.kapitencraft.scripted.code.exe.methods.builder.consumer;

import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.util.Consumers;

public class CB10P<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> {
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;
    private final ParamInst<P3> param3;
    private final ParamInst<P4> param4;
    private final ParamInst<P5> param5;
    private final ParamInst<P6> param6;
    private final ParamInst<P7> param7;
    private final ParamInst<P8> param8;
    private final ParamInst<P9> param9;
    private final ParamInst<P10> param10;
    //private final DoubleMap<VarType<?>, String, MB9P<R, P1, P2, P3, P4, P5, P6, P7, P8, ?>> children = new DoubleMap<>();

    private Consumers.C10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> executor;

    public CB10P(ParamInst<P1> param1, ParamInst<P2> param2, ParamInst<P3> param3, ParamInst<P4> param4, ParamInst<P5> param5, ParamInst<P6> param6, ParamInst<P7> param7, ParamInst<P8> param8, ParamInst<P9> param9, ParamInst<P10> param10) {
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.param4 = param4;
        this.param5 = param5;
        this.param6 = param6;
        this.param7 = param7;
        this.param8 = param8;
        this.param9 = param9;
        this.param10 = param10;
    }

//    public <P9> MB9P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9> withParam(String name, Supplier<? extends VarType<P9>> type) {
//        return (MB9P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9>) this.children.computeIfAbsent(type.get(), name, (type1, string) -> new MB9P<>(retType, param1, param2, param3, param4, param5, param6, param7, param8, new ParamInst<>(type1, string)));
//    }

    public CB10P<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> executes(Consumers.C10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> executor) {
        if (this.executor != null) throw new IllegalStateException("executor has already been set");
        this.executor = executor;
        return this;
    }
}
