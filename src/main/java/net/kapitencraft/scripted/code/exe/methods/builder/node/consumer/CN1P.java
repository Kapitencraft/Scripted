package net.kapitencraft.scripted.code.exe.methods.builder.node.consumer;

import com.google.gson.JsonObject;
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
import java.util.function.Consumer;

public class CN1P<P1> implements ReturningNode<Void> {
    private final ParamInst<P1> param1;

    private final Consumer<P1> executor;

    public CN1P(ParamInst<P1> param1, Consumer<P1> executor) {
        this.param1 = param1;
        this.executor = executor;
    }

    @Override
    public MethodInstance<Void> createInst(String methodId, List<MethodInstance<?>> params) {
        return new Instance(methodId,
                (MethodInstance<P1>) params.get(0)
        );
    }

    @Override
    public int getParamCount() {
        return 1;
    }

    @Override
    public MethodInstance<Void> loadInst(JsonObject object, VarAnalyser analyser) {
        return new Instance(GsonHelper.getAsString(object, "type"),
                Method.loadInstance(object, param1.name(), analyser)
        );
    }

    @Override
    public boolean matchesTypes(List<? extends VarType<?>> types) {
        return true;
    }

    private class Instance extends MethodInstance<Void> {
        private final MethodInstance<P1> param1;

        protected Instance(String id, MethodInstance<P1> param1) {
            super(id);
            this.param1 = param1;
        }

        @Override
        public Void call(VarMap origin, MethodPipeline<?> pipeline) {
            executor.accept(param1.call(origin, pipeline));
            return null; //always return null
        }

        @Override
        public VarType<Void> getType(IVarAnalyser analyser) {
            return VarTypes.VOID.get();
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add(CN1P.this.param1.name(), param1.toJson());
        }
    }
}
