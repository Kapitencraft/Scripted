package net.kapitencraft.scripted.code.exe.methods.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.ModVarTypes;

public class WhenMethod<T> extends Method<T> {
    public WhenMethod() {
        super(ParamSet.single(ParamSet.builder().addParam("condition", ModVarTypes.BOOL).addWildCardParam("ifTrue", "ifFalse").addOptionalWildCardParam("ifFalse", "ifTrue")), "when");
    }

    @Override
    public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data);
    }

    public class Instance extends Method<T>.Instance {

        protected Instance(ParamData paramData) {
            super(paramData);
        }

        @Override
        protected Var<T> call(VarMap params) {
            if (params.getVarValue("condition", ModVarTypes.BOOL)) {
                return params.getVar("ifTrue");
            } else {
                return params.hasVar("ifFalse") ? params.getVar("ifFalse") : (Var<T>) new Var<>(params.getVar("ifTrue").getType());
            }
        }

        @Override
        public VarType<T> getType(VarAnalyser analyser) {
            return (VarType<T>) analyser.getType("ifTrue");
        }
    }
}