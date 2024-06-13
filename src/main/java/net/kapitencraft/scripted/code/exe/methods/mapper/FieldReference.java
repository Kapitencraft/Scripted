package net.kapitencraft.scripted.code.exe.methods.mapper;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.oop.Field;
import net.kapitencraft.scripted.code.oop.InstanceMethod;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import org.jetbrains.annotations.Nullable;

public final class FieldReference<P, T> extends InstanceMethod<P, T> {

    public FieldReference() {
        super(ParamSet.empty(), "field");
    }

    @Override
    public InstanceMethod<P, T>.Instance load(ParamData set, Method<P>.Instance inst, JsonObject object) {
        throw new IllegalStateException("do not load a Field Reference directly; use 'References.FIELD.load()' instead");
    }

    public <J> InstanceMethod<?,?>.Instance load(Field<J,?> fieldForName, VarReference<J>.Instance instance) {
        return new Instance((Field<P, T>) fieldForName, (VarReference<P>.Instance) instance);
    }

    public @Nullable Method<?>.Instance create(Field<P, T> field, VarReference<P>.Instance parent) {
        return new Instance(field, parent);
    }


    public class Instance extends InstanceMethod<P, T>.Instance {
        private final Field<P, T> field;

        protected Instance(Field<P, T> field, VarReference<P>.Instance parent) {
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
