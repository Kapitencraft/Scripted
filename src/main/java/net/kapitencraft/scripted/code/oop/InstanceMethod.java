package net.kapitencraft.scripted.code.oop;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import org.jetbrains.annotations.NotNull;

public abstract class InstanceMethod<P, K> extends Method<K> {

    protected InstanceMethod(ParamSet builder, String name) {
        super(builder, name);
    }

    public abstract InstanceMethod<P, K>.Instance load(ParamData set, Method<P>.Instance inst, JsonObject object);

    @Override
    public Method<K>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        throw new JsonSyntaxException("do not load an Instance Method directly");
    }

    public abstract class Instance extends Method<K>.Instance {
        private final @NotNull Method<P>.Instance parent;

        protected Instance(ParamData paramData, @NotNull Method<P>.Instance parent) {
            super(paramData);
            this.parent = parent;
        }

        @Override
        public Var<K> call(VarMap params) {
            return this.callInit(map -> this.call(map, parent.callInit(params)), params);
        }

        public abstract Var<K> call(VarMap map, Var<P> inst);
    }
}
