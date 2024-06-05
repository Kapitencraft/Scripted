package net.kapitencraft.scripted.code.exe.functions.abstracts;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

public abstract class InstanceFunction<T> extends Function {

    public final Instance load(JsonObject object, VarAnalyser analyser) {
        Method<T>.Instance method = Method.loadFromSubObject(object, "supplier", analyser);
        return loadInstance(object, analyser, method);
    }

    public abstract Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<T>.Instance inst);

    public abstract class Instance extends Function.Instance {
        private final Method<T>.Instance supplier;

        protected Instance(Method<T>.Instance supplier) {
            this.supplier = supplier;
        }

        @Override
        public final void execute(VarMap map, MethodPipeline<?> source) {
            executeInstanced(map, source, supplier.callInit(map));
        }

        public abstract void executeInstanced(VarMap map, MethodPipeline<?> source, Var<T> instance);

        @Override
        public void analyse(VarAnalyser analyser) {
            this.supplier.analyse(analyser);
        }

        @Override
        public void save(JsonObject object) {
            object.add("supplier", supplier.toJson());
        }
    }
}