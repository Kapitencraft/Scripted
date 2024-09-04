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
import java.util.function.BiFunction;

public class MN2P<R, P1, P2> implements ReturningNode<R> {
    private final VarType<R> retType;
    private final ParamInst<P1> param1;
    private final ParamInst<P2> param2;

    private final @Nullable BiFunction<P1, P2, R> executor;

    public MN2P(VarType<R> retType, ParamInst<P1> param1, ParamInst<P2> param2, @Nullable BiFunction<P1, P2, R> executor) {
        this.retType = retType;
        this.param1 = param1;
        this.param2 = param2;
        this.executor = executor;
    }

    @Override
    public List<? extends VarType<?>> getTypes() {
        return List.of(
                param1.type(),
                param2.type()
        );
    }

    public MethodInstance<R> loadInst(JsonObject object, VarAnalyser analyser) {
        if (executor == null) throw new IllegalAccessError("can not create a Method without executor");
        return new Instance(GsonHelper.getAsString(object, "type"),
                Method.loadInstance(object, param1.name(), analyser),
                Method.loadInstance(object, param2.name(), analyser)
        );
    }

    @Override
    public boolean matchesTypes(List<? extends VarType<?>> types) {
        return param1.type() == types.get(0) && param2.type() == types.get(1);
    }

    public MethodInstance<R> createInst(String id, List<MethodInstance<?>> params) {
        return create(id, (MethodInstance<P1>) params.get(0), (MethodInstance<P2>) params.get(1));
    }

    public MethodInstance<R> create(String id, MethodInstance<P1> param1Inst, MethodInstance<P2> param2Inst) {
        return new Instance(id, param1Inst, param2Inst);
    }

    @Override
    public int getParamCount() {
        return 2;
    }

    private class Instance extends MethodInstance<R> {
        private final MethodInstance<P1> param1;
        private final MethodInstance<P2> param2;

        private Instance(String id, MethodInstance<P1> param1, MethodInstance<P2> param2) {
            super(id);
            this.param1 = param1;
            this.param2 = param2;
        }

        @Override
        public R call(VarMap origin, MethodPipeline<?> pipeline) {
            return Objects.requireNonNull(executor, "found method without executor!")
                    .apply(
                            this.param1.call(origin, pipeline),
                            this.param2.call(origin, pipeline)
                    );
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add(MN2P.this.param1.name(), param1.toJson());
            object.add(MN2P.this.param2.name(), param2.toJson());
        }

        @Override
        public VarType<R> getType(IVarAnalyser analyser) {
            return retType;
        }
    }
}
