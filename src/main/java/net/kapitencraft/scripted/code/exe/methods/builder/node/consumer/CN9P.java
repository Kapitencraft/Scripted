package net.kapitencraft.scripted.code.exe.methods.builder.node.consumer;

import com.google.gson.JsonObject;
import net.kapitencraft.kap_lib.stream.Consumers;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.util.GsonHelper;

import java.util.List;

public class CN9P<P1, P2, P3, P4, P5, P6, P7, P8, P9> implements ReturningNode<Void> {
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;
    private final ParamInst<P3> param3;
    private final ParamInst<P4> param4;
    private final ParamInst<P5> param5;
    private final ParamInst<P6> param6;
    private final ParamInst<P7> param7;
    private final ParamInst<P8> param8;
    private final ParamInst<P9> param9;

    private final Consumers.C9<P1, P2, P3, P4, P5, P6, P7, P8, P9> executor;

    public CN9P(ParamInst<P1> param1, ParamInst<P2> param2, ParamInst<P3> param3, ParamInst<P4> param4, ParamInst<P5> param5, ParamInst<P6> param6, ParamInst<P7> param7, ParamInst<P8> param8, ParamInst<P9> param9, Consumers.C9<P1, P2, P3, P4, P5, P6, P7, P8, P9> executor) {
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.param4 = param4;
        this.param5 = param5;
        this.param6 = param6;
        this.param7 = param7;
        this.param8 = param8;
        this.param9 = param9;
        this.executor = executor;
    }

    @Override
    public MethodInstance<Void> createInst(String methodId, List<MethodInstance<?>> params) {
        return new Instance(methodId,
                (MethodInstance<P1>) params.get(0), (MethodInstance<P2>) params.get(1),
                (MethodInstance<P3>) params.get(2), (MethodInstance<P4>) params.get(3),
                (MethodInstance<P5>) params.get(4), (MethodInstance<P6>) params.get(5),
                (MethodInstance<P7>) params.get(6), (MethodInstance<P8>) params.get(7),
                (MethodInstance<P9>) params.get(8)
        );
    }

    @Override
    public int getParamCount() {
        return 8;
    }

    @Override
    public MethodInstance<Void> loadInst(JsonObject object, VarAnalyser analyser) {
        return new Instance(GsonHelper.getAsString(object, "type"),
                Method.loadInstance(object, param1.name(), analyser),
                Method.loadInstance(object, param2.name(), analyser),
                Method.loadInstance(object, param3.name(), analyser),
                Method.loadInstance(object, param4.name(), analyser),
                Method.loadInstance(object, param5.name(), analyser),
                Method.loadInstance(object, param6.name(), analyser),
                Method.loadInstance(object, param7.name(), analyser),
                Method.loadInstance(object, param8.name(), analyser),
                Method.loadInstance(object, param9.name(), analyser)
        );
    }

    @Override
    public boolean matchesTypes(List<? extends VarType<?>> types) {
        return true;
    }

    private class Instance extends MethodInstance<Void> {
        private final MethodInstance<P1> param1;
        private final MethodInstance<P2> param2;
        private final MethodInstance<P3> param3;
        private final MethodInstance<P4> param4;
        private final MethodInstance<P5> param5;
        private final MethodInstance<P6> param6;
        private final MethodInstance<P7> param7;
        private final MethodInstance<P8> param8;
        private final MethodInstance<P9> param9;

        protected Instance(String id, MethodInstance<P1> param1, MethodInstance<P2> param2, MethodInstance<P3> param3, MethodInstance<P4> param4, MethodInstance<P5> param5, MethodInstance<P6> param6, MethodInstance<P7> param7, MethodInstance<P8> param8, MethodInstance<P9> param9) {
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
        }

        @Override
        public Void call(VarMap origin, MethodPipeline<?> pipeline) {
            executor.apply(param1.call(origin, pipeline), param2.call(origin, pipeline),
                    param3.call(origin, pipeline), param4.call(origin, pipeline),
                    param5.call(origin, pipeline), param6.call(origin, pipeline),
                    param7.call(origin, pipeline), param8.call(origin, pipeline),
                    param9.call(origin, pipeline)
            );
            return null; //always return null
        }

        @Override
        public VarType<Void> getType(IVarAnalyser analyser) {
            return VarTypes.VOID.get();
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add(CN9P.this.param1.name(), param1.toJson());
            object.add(CN9P.this.param2.name(), param2.toJson());
            object.add(CN9P.this.param3.name(), param3.toJson());
            object.add(CN9P.this.param4.name(), param4.toJson());
            object.add(CN9P.this.param5.name(), param5.toJson());
            object.add(CN9P.this.param6.name(), param6.toJson());
            object.add(CN9P.this.param7.name(), param7.toJson());
            object.add(CN9P.this.param8.name(), param8.toJson());
            object.add(CN9P.this.param9.name(), param9.toJson());
        }
    }
}
