package net.kapitencraft.scripted.code.exe.methods.builder.node.method;

import com.google.gson.JsonObject;
import net.kapitencraft.kap_lib.stream.Functions;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class MN10P<R, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> implements ReturningNode<R> {
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
    private final ParamInst<P10> param10;

    private final @Nullable Functions.F10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> executor;

    public MN10P(VarType<R> retType, ParamInst<P1> param1, ParamInst<P2> param2, ParamInst<P3> param3, ParamInst<P4> param4, ParamInst<P5> param5, ParamInst<P6> param6, ParamInst<P7> param7, ParamInst<P8> param8, ParamInst<P9> param9, ParamInst<P10> param10, @Nullable Functions.F10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> executor) {
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
        this.param10 = param10;
        this.executor = executor;
    }

    @Override
    public List<? extends VarType<?>> getTypes() {
        return List.of(
                param1.type(),
                param2.type(),
                param3.type(),
                param4.type(),
                param5.type(),
                param6.type(),
                param7.type(),
                param8.type(),
                param9.type(),
                param10.type()
        );
    }

    public MethodInstance<R> loadInst(JsonObject object, VarAnalyser analyser) {
        if (executor == null) throw new IllegalAccessError("can not create a Method without executor");
        return new Instance(GsonHelper.getAsString(object, "type"),
                Method.loadInstance(object, param1.name(), analyser),
                Method.loadInstance(object, param2.name(), analyser),
                Method.loadInstance(object, param3.name(), analyser),
                Method.loadInstance(object, param4.name(), analyser),
                Method.loadInstance(object, param5.name(), analyser),
                Method.loadInstance(object, param6.name(), analyser),
                Method.loadInstance(object, param7.name(), analyser),
                Method.loadInstance(object, param8.name(), analyser),
                Method.loadInstance(object, param9.name(), analyser),
                Method.loadInstance(object, param10.name(), analyser)
        );
    }

    @Override
    public boolean matchesTypes(List<? extends VarType<?>> types) {
        return param1.type() == types.get(0) &&
                param2.type() == types.get(1) &&
                param3.type() == types.get(2) &&
                param4.type() == types.get(3) &&
                param5.type() == types.get(4) &&
                param6.type() == types.get(5) &&
                param7.type() == types.get(6) &&
                param8.type() == types.get(7) &&
                param9.type() == types.get(8) &&
                param10.type() == types.get(9);
    }

    public MethodInstance<R> createInst(String id, List<MethodInstance<?>> params) {
        return create(id, (MethodInstance<P1>) params.get(0), (MethodInstance<P2>) params.get(1),
                (MethodInstance<P3>) params.get(2), (MethodInstance<P4>) params.get(3),
                (MethodInstance<P5>) params.get(4), (MethodInstance<P6>) params.get(5),
                (MethodInstance<P7>) params.get(6), (MethodInstance<P8>) params.get(7),
                (MethodInstance<P9>) params.get(8), (MethodInstance<P10>) params.get(9));
    }

    public MethodInstance<R> create(String id, MethodInstance<P1> param1Inst, MethodInstance<P2> param2Inst,
                                    MethodInstance<P3> param3Inst, MethodInstance<P4> param4Inst,
                                    MethodInstance<P5> param5Inst, MethodInstance<P6> param6Inst,
                                    MethodInstance<P7> param7Inst, MethodInstance<P8> param8Inst,
                                    MethodInstance<P9> param9Inst, MethodInstance<P10> param10Inst) {
        return new Instance(id, param1Inst, param2Inst, param3Inst, param4Inst, param5Inst, param6Inst, param7Inst, param8Inst, param9Inst, param10Inst);
    }

    @Override
    public int getParamCount() {
        return 2;
    }

    private class Instance extends MethodInstance<R> {
        private final MethodInstance<P1> param1;
        private final MethodInstance<P2> param2;
        private final MethodInstance<P3> param3;
        private final MethodInstance<P4> param4;
        private final MethodInstance<P5> param5;
        private final MethodInstance<P6> param6;
        private final MethodInstance<P7> param7;
        private final MethodInstance<P8> param8;
        private final MethodInstance<P9> param9;
        private final MethodInstance<P10> param10;

        private Instance(String id, MethodInstance<P1> param1, MethodInstance<P2> param2, MethodInstance<P3> param3, MethodInstance<P4> param4, MethodInstance<P5> param5, MethodInstance<P6> param6, MethodInstance<P7> param7, MethodInstance<P8> param8, MethodInstance<P9> param9, MethodInstance<P10> param10) {
            super(id);
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

        @Override
        public R call(VarMap origin, MethodPipeline<?> pipeline) {
            return Objects.requireNonNull(executor, "found method without executor!")
                    .apply(
                            this.param1.call(origin, pipeline),
                            this.param2.call(origin, pipeline),
                            this.param3.call(origin, pipeline),
                            this.param4.call(origin, pipeline),
                            this.param5.call(origin, pipeline),
                            this.param6.call(origin, pipeline),
                            this.param7.call(origin, pipeline),
                            this.param8.call(origin, pipeline),
                            this.param9.call(origin, pipeline),
                            this.param10.call(origin, pipeline)
                    );
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add(MN10P.this.param1.name(), param1.toJson());
            object.add(MN10P.this.param2.name(), param2.toJson());
            object.add(MN10P.this.param3.name(), param3.toJson());
            object.add(MN10P.this.param4.name(), param4.toJson());
            object.add(MN10P.this.param5.name(), param5.toJson());
            object.add(MN10P.this.param6.name(), param6.toJson());
            object.add(MN10P.this.param7.name(), param7.toJson());
            object.add(MN10P.this.param8.name(), param8.toJson());
            object.add(MN10P.this.param9.name(), param9.toJson());
            object.add(MN10P.this.param10.name(), param10.toJson());
        }

        @Override
        public VarType<R> getType(IVarAnalyser analyser) {
            return retType;
        }
    }
}
