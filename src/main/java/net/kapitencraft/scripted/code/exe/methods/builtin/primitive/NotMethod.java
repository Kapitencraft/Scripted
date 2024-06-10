package net.kapitencraft.scripted.code.exe.methods.builtin.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.ModVarTypes;

public class NotMethod extends Method<Boolean> {
    public NotMethod() {
        super(ParamSet.single(ParamSet.builder().addParam("val", ModVarTypes.BOOL)), "not");
    }

    @Override
    public Method<Boolean>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data);
    }

    public class Instance extends Method<Boolean>.Instance {

        protected Instance(ParamData paramData) {
            super(paramData);
        }

        @Override
        protected Var<Boolean> call(VarMap params) {
            return new Var<>(ModVarTypes.BOOL.get(), !params.getVarValue("val", ModVarTypes.BOOL));
        }

        @Override
        public VarType<Boolean> getType(VarAnalyser analyser) {
            return ModVarTypes.BOOL.get();
        }
    }
}
