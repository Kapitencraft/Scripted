package net.kapitencraft.scripted.code.exe.methods.builtin.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;

public class NotMethod extends Method<Boolean> {

    @Override
    public MethodInstance<Boolean> load(JsonObject object, VarAnalyser analyser) {
        return new Instance(Method.loadInstance(object, "val", analyser));
    }

    public MethodInstance<?> create(MethodInstance<?> methodInstance) {
        return new Instance((MethodInstance<Boolean>) methodInstance);
    }

    public static class Instance extends MethodInstance<Boolean> {
        private final MethodInstance<Boolean> parent;

        public Instance(MethodInstance<Boolean> parent) {
            super("not"); //TODO make default system for registered method types
            this.parent = parent;
        }

        @Override
        public Boolean call(VarMap origin, MethodPipeline<?> pipeline) {
            return !parent.call(origin, pipeline);
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add("val", this.parent.toJson());
        }

        @Override
        public VarType<Boolean> getType(IVarAnalyser analyser) {
            return VarTypes.BOOL.get();
        }
    }
}