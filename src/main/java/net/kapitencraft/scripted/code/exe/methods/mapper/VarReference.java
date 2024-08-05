package net.kapitencraft.scripted.code.exe.methods.mapper;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

public class VarReference<T> extends Method<T> {
    public VarReference() {
        super(ParamSet.empty(), "var");
    }

    public VarReference<T>.Instance load(String name) {
        return new Instance(name);
    }

    @Override
    public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        throw new JsonSyntaxException("do not load Var References directly");
    }

    public Method<?>.Instance create(String s) {
        return new Instance(s);
    }

    public class Instance extends Method<T>.Instance implements IVarReference {
        private final String methodName;

        protected Instance(String methodName) {
            super(null);
            this.methodName = methodName;
        }

        @Override
        public T call(VarMap params, VarMap origin) {
            return params.getVarValue(methodName, () -> getType(params));
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            analyser.assertVarExistence(methodName);
        }

        @Override
        public VarType<T> getType(IVarAnalyser analyser) {
            return analyser.getType(methodName);
        }
    }
}
