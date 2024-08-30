package net.kapitencraft.scripted.code.exe.methods.builder.node.method;

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
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class MN1P<R, P1> implements ReturningNode<R> {
    private final VarType<R> retType;
    private final ParamInst<P1> param1;

    private final @Nullable Function<P1, R> executor;

    public MN1P(VarType<R> retType, ParamInst<P1> param1, @Nullable Function<P1, R> executor) {
        this.retType = retType;
        this.param1 = param1;
        this.executor = executor;
    }

    public MethodInstance<R> read(JsonObject object, VarAnalyser analyser) {
        if (executor == null) throw new IllegalAccessError("can not create a Method without executor");
        return new Instance(Method.loadInstance(GsonHelper.getAsJsonObject(object, param1.name()), analyser));
    }

    public MethodInstance<R> createInst(List<MethodInstance<?>> params) {
        return create((MethodInstance<P1>) params.get(0));
    }

    public MethodInstance<R> create(MethodInstance<P1> param1Inst) {
        return new Instance(param1Inst);
    }

    @Override
    public int getParamCount() {
        return 1;
    }

    private class Instance extends MethodInstance<R> {
        private final MethodInstance<P1> param1;

        private Instance(MethodInstance<P1> param1) {
            this.param1 = param1;
        }

        @Override
        public R call(VarMap origin, MethodPipeline<?> pipeline) {
            return Objects.requireNonNull(executor, "found method without executor!").apply(this.param1.call(origin, pipeline));
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add(MN1P.this.param1.name(), param1.toJson());
        }

        @Override
        public VarType<R> getType(IVarAnalyser analyser) {
            return retType;
        }
    }
}
