package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;

public class CreateAndSetVarFunction extends SetVarFunction {


    @Override
    public Instance<?> load(JsonObject object, VarAnalyser analyser) {
        return read(object, analyser);
    }

    private <T> Instance<T> read(JsonObject object, VarAnalyser analyser) {
        String name = GsonHelper.getAsString(object, "name");
        Method<T>.Instance method = Method.loadFromSubObject(object, "val", analyser);
        VarType<T> type = JsonHelper.readType(object, "var_type");
        return new Instance<>(name, method, type);
    }

    public class Instance<T> extends SetVarFunction.Instance<T> {
        private final VarType<T> type;

        public Instance(String varName, Method<T>.Instance method, VarType<T> type) {
            super(varName, method);
            this.type = type;
        }

        @Override
        public void save(JsonObject object) {
            super.save(object);
            object.addProperty("var_type", JsonHelper.saveType(type));
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            analyser.registerVar(varName, type);
        }
    }
}