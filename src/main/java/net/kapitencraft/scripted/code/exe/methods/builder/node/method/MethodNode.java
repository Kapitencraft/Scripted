package net.kapitencraft.scripted.code.exe.methods.builder.node.method;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class MethodNode<R> implements ReturningNode<R> {
    private final VarType<R> retType;
    private final @Nullable Supplier<R> executor;

    public MethodNode(VarType<R> retType, @Nullable Supplier<R> executor) {
        this.retType = retType;
        this.executor = executor;
    }

    public MethodInstance<R> createInst(String methodId, List<MethodInstance<?>> ignored) {
        if (executor == null) throw new IllegalAccessError("can not create a Method without executor");
        return new Instance(methodId);
    }

    @Override
    public MethodInstance<R> loadInst(JsonObject object, VarAnalyser analyser) {
        return new Instance(GsonHelper.getAsString(object, "type"));
    }

    @Override
    public boolean matchesTypes(List<? extends VarType<?>> types) {
        return true;
    }

    @Override
    public int getParamCount() {
        return 0;
    }

    private final class Instance extends MethodInstance<R> {

        private Instance(String id) {
            super(id);
        }

        @Override
        public R call(VarMap origin, MethodPipeline<?> pipeline) {
            return Objects.requireNonNull(executor, "found method without executor!").get();
        }

        @Override
        public VarType<R> getType(IVarAnalyser analyser) {
            return retType;
        }
    }
}
