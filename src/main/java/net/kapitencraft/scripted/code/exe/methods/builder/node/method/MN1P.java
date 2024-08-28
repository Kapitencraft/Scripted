package net.kapitencraft.scripted.code.exe.methods.builder.node.method;

import com.google.gson.JsonObject;
import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.util.GsonHelper;

import java.util.function.Function;

public class MN1P<R, P1> implements InstMapper<P1, R>, Returning<R> {
    private final VarType<R> retType;
    private final ParamInst<P1> param1;
    private final DoubleMap<VarType<?>, String, MN2P<R, P1, ?>> children = new DoubleMap<>();

    private final Function<P1, R> executor;

    public MN1P(VarType<R> retType, ParamInst<P1> param1, Function<P1, R> executor) {
        this.retType = retType;
        this.param1 = param1;
        this.executor = executor;
    }

    public MethodInstance<R> read(JsonObject object, VarAnalyser analyser) {
        return new Instance(Method.loadInstance(GsonHelper.getAsJsonObject(object, "param1"), analyser));
    }

    public MethodInstance<R> create(MethodInstance<P1> param1Inst) {
        return new Instance(param1Inst);
    }

    private class Instance extends MethodInstance<R> {
        private final MethodInstance<P1> parent;

        private Instance(MethodInstance<P1> parent) {
            this.parent = parent;
        }

        @Override
        public R call(VarMap origin, MethodPipeline<?> pipeline) {
            return executor.apply(this.parent.call(origin, pipeline));
        }

        @Override
        public VarType<R> getType(IVarAnalyser analyser) {
            return retType;
        }
    }
}
