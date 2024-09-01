package net.kapitencraft.scripted.code.exe.methods.mapper;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

public class VarReference<T> extends Method<T> {

    public VarReference<T>.Instance load(String name) {
        return new Instance(name);
    }

    @Override
    public MethodInstance<T> load(JsonObject object, VarAnalyser analyser) {
        throw new JsonSyntaxException("do not load Var References directly");
    }

    public MethodInstance<?> create(String s) {
        return new Instance(s);
    }

    public class Instance extends MethodInstance<T> implements IVarReference {
        private final String methodName;

        protected Instance(String methodName) {
            super("Var-" + methodName);
            this.methodName = methodName;
        }

        @Override
        public T call(VarMap origin, MethodPipeline<?> pipeline) {
            return origin.getVarValue(methodName, () -> getType(origin));
        }

        @Override
        public Var<T> buildVar(VarMap origin, MethodPipeline<?> pipeline) {
            return origin.getVar(methodName);
        }

        @Override
        public VarType<T> getType(IVarAnalyser analyser) {
            return analyser.getType(methodName);
        }
    }
}
