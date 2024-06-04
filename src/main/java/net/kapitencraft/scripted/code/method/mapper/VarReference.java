package net.kapitencraft.scripted.code.method.mapper;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.scripted.code.method.Method;
import net.kapitencraft.scripted.code.method.param.ParamSet;
import net.kapitencraft.scripted.code.method.param.ParamData;
import net.kapitencraft.scripted.code.oop.InstanceMethod;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.minecraft.util.GsonHelper;

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

    public class Instance extends Method<T>.Instance {
        private final String methodName;

        protected Instance(String methodName) {
            super(null);
            this.methodName = methodName;
        }

        @Override
        public Var<T> call(VarMap params) {
            return params.getVar(methodName);
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            analyser.assertVarExistence(methodName);
        }

        @Override
        public InstanceMethod<?, ?>.Instance loadChild(JsonObject then, VarAnalyser analyser) {
            if (!then.has("params")) {
                String fieldName = GsonHelper.getAsString(then, "name");
                return References.FIELD.load(this.getType(analyser).getFieldForName(fieldName), this);
            } else {
                return super.loadChild(then, analyser);
            }

        }

        @Override
        public VarType<T> getType(VarAnalyser analyser) {
            return analyser.getVar(methodName);
        }
    }
}
