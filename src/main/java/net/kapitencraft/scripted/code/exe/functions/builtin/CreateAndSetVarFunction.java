package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;

public class CreateAndSetVarFunction extends Function {

    @Override
    public Instance<?> load(JsonObject object, VarAnalyser analyser) {
        return read(object, analyser);
    }

    private <T> Instance<T> read(JsonObject object, VarAnalyser analyser) {
        String name = GsonHelper.getAsString(object, "name");
        MethodInstance<T> method = Method.loadFromSubObject(object, "val", analyser);
        VarType<T> type = JsonHelper.readType(object, "var_type");
        return create(name, method, type, false);
    }

    public <T> Instance<T> create(String name, MethodInstance<T> creator, VarType<T> type, boolean isFinal) {
        return new Instance<>(name, creator, type, isFinal);
    }

    public class Instance<T> extends Function.Instance {
        private final String varName;
        private final VarType<T> type;
        private final boolean isFinal;
        private final MethodInstance<T> setter;

        public Instance(String varName, MethodInstance<T> method, VarType<T> type, boolean isFinal) {
            super("createAndSet");
            this.varName = varName;
            this.type = type;
            this.isFinal = isFinal;
            this.setter = method;
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.addProperty("var_name", varName);
            object.addProperty("var_type", JsonHelper.saveType(type));
            object.addProperty("isFinal", isFinal);
            object.add("setter", setter.toJson());
        }

        @Override
        public void execute(VarMap origin, MethodPipeline<?> pipeline) {
            origin.addVarType(varName, type, isFinal);
            origin.getVar(varName).setValue(setter.call(origin, pipeline));
        }
    }
}