package net.kapitencraft.scripted.code.exe.methods.mapper;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import org.jetbrains.annotations.Nullable;

public final class FieldReference<P, T> extends VarType<P>.InstanceMethod<T> {

    public FieldReference() {
        super(ParamSet.empty(), "field");
    }

    @Override
    public VarType<P>.InstanceMethod<T>.Instance load(ParamData data, Method<P>.Instance inst, JsonObject object) {
        throw new IllegalStateException("do not load a Field Reference directly; use 'References.FIELD.load()' instead");
    }

    public <J> VarType<?>.InstanceMethod<?>.Instance load(VarType<J>.Field<?> fieldForName, VarReference<J>.Instance instance) {
        return new Instance((VarType<P>.Field<T>) fieldForName, (VarReference<P>.Instance) instance);
    }

    public @Nullable Method<T>.Instance create(VarType<?>.Field<?> field, VarReference<?>.Instance parent) {
        return new Instance((VarType<P>.Field<T>) field, (VarReference<P>.Instance) parent);
    }

    @Override
    protected Method<T>.Instance create(ParamData data) {
        throw new IllegalStateException("do not create Field Reference directly");
    }


    public class Instance extends VarType<P>.InstanceMethod<T>.Instance {
        private final VarType<P>.Field<T> field;

        protected Instance(VarType<P>.Field<T> field, VarReference<P>.Instance parent) {
            super(null, parent);
            this.field = field;
        }

        @Override
        public Var<T> call(VarMap map, Var<P> inst) {
            return new Var<>(field.getType(), field.getValue(inst.getValue()), true);
        }

        @Override
        public VarType<T> getType(VarAnalyser analyser) {
            return field.getType();
        }
    }
}
