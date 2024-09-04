package net.kapitencraft.scripted.code.exe.methods.builder.node.consumer;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.util.GsonHelper;

import java.util.List;

public class ConsumerNode implements ReturningNode<Void> {
    private final Runnable executor;

    public ConsumerNode(Runnable executor) {
        this.executor = executor;
    }

    @Override
    public MethodInstance<Void> createInst(String methodId, List<MethodInstance<?>> params) {
        return new Instance(methodId);
    }

    @Override
    public int getParamCount() {
        return 0;
    }

    @Override
    public MethodInstance<Void> loadInst(JsonObject object, VarAnalyser analyser) {
        return new Instance(GsonHelper.getAsString(object, "type"));
    }

    @Override
    public List<? extends VarType<?>> getTypes() {
        return List.of();
    }

    @Override
    public boolean matchesTypes(List<? extends VarType<?>> types) {
        return true;
    }

    private class Instance extends MethodInstance<Void> {

        protected Instance(String id) {
            super(id);
        }

        @Override
        public Void call(VarMap origin, MethodPipeline<?> pipeline) {
            executor.run();
            return null; //always return null
        }

        @Override
        public VarType<Void> getType(IVarAnalyser analyser) {
            return VarTypes.VOID.get();
        }
    }
}
