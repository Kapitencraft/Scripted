package net.kapitencraft.scripted.code.exe.methods.builtin.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;

public class NotMethod extends Method<Boolean> {

    public NotMethod() {
        super(set -> set.addEntry(entry -> entry
                .addParam("val", VarTypes.BOOL)
        ), null);
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
        protected Boolean call(VarMap params, VarMap origin) {
            return !params.getVarValue("val", VarTypes.BOOL);
        }

        @Override
        public VarType<Boolean> getType(IVarAnalyser analyser) {
            return VarTypes.BOOL.get();
        }
    }
}