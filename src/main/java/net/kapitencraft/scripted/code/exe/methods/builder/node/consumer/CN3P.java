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

public class CN3P<P1, P2, P3> implements ReturningNode<Void> {
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;
    private final ParamInst<P3> param3;

    private final Consumers.C3<P1, P2, P3> executor;

    public CN3P(ParamInst<P1> param1, ParamInst<P2> param2, ParamInst<P3> param3, Consumers.C3<P1, P2, P3> executor) {
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.executor = executor;
    }

    @Override
    public List<? extends VarType<?>> getTypes() {
        return List.of(
                param1.type(),
                param2.type(),
                param3.type()
        );
    }

    @Override
    public MethodInstance<Void> createInst(String methodId, List<MethodInstance<?>> params) {
        return new Instance(methodId,
                (MethodInstance<P1>) params.get(0), (MethodInstance<P2>) params.get(1), (MethodInstance<P3>) params.get(2)
        );
    }

    @Override
    public int getParamCount() {
        return 3;
    }

    @Override
    public MethodInstance<Void> loadInst(JsonObject object, VarAnalyser analyser) {
        return new Instance(GsonHelper.getAsString(object, "type"),
                Method.loadInstance(object, param1.name(), analyser),
                Method.loadInstance(object, param2.name(), analyser),
                Method.loadInstance(object, param3.name(), analyser)
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

        protected Instance(String id, MethodInstance<P1> param1, MethodInstance<P2> param2, MethodInstance<P3> param3) {
            super(id);
            this.param1 = param1;
            this.param2 = param2;
            this.param3 = param3;
        }

        @Override
        public Void call(VarMap origin, MethodPipeline<?> pipeline) {
            executor.apply(param1.call(origin, pipeline), param2.call(origin, pipeline), param3.call(origin, pipeline));
            return null; //always return null
        }

        @Override
        public VarType<Void> getType(IVarAnalyser analyser) {
            return VarTypes.VOID.get();
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add(CN3P.this.param1.name(), param1.toJson());
            object.add(CN3P.this.param2.name(), param2.toJson());
            object.add(CN3P.this.param3.name(), param3.toJson());
        }
    }
}
