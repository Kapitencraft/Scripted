package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;

public class CreateAndSetVarFunction extends Function {

    public CreateAndSetVarFunction() {
        super(ParamSet.empty(), "%ignored");
    }

    @Override
    public Instance<?> load(JsonObject object, VarAnalyser analyser) {
        return read(object, analyser);
    }

    @Override
    public Method<Void>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return read(object, analyser);
    }

    private <T> Instance<T> read(JsonObject object, VarAnalyser analyser) {
        String name = GsonHelper.getAsString(object, "name");
        Method<T>.Instance method = Method.loadFromSubObject(object, "val", analyser);
        VarType<T> type = JsonHelper.readType(object, "var_type");
        return create(name, method, type, false);
    }

    public <T> Instance<T> create(String name, Method<T>.Instance creator, VarType<T> type, boolean isFinal) {
        return new Instance<>(name, creator, type, isFinal);
    }

    public class Instance<T> extends Function.Instance {
        private final String varName;
        private final VarType<T> type;
        private final boolean isFinal;
        private final Method<T>.Instance setter;

        public Instance(String varName, Method<T>.Instance method, VarType<T> type, boolean isFinal) {
            super(ParamData.empty());
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
        public void execute(VarMap map, MethodPipeline<?> source) {
            VarMap sourceMap = source.getMap();
            sourceMap.addVarType(varName, type, isFinal);
            sourceMap.getVar(varName).setValue(setter.callInit(sourceMap));
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            analyser.registerVar(varName, type);
        }
    }
}