package net.kapitencraft.scripted.code.method.elements.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.method.Method;
import net.kapitencraft.scripted.code.method.MethodPipeline;
import net.kapitencraft.scripted.code.method.elements.abstracts.Function;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;
import net.minecraft.util.GsonHelper;

public class SetVarFunction extends Function {

    @Override
    public Instance<?> load(JsonObject object, VarAnalyser analyser) {
        String name = GsonHelper.getAsString(object, "name");
        Method<?> method = Method.loadFromSubObject(object, "val", analyser);
        return new Instance<>(name, method);
    }

    public class Instance<T> extends Function.Instance {
        protected final String varName;
        private final Method<T> method;

        public Instance(String varName, Method<T> method) {
            this.varName = varName;
            this.method = method;
        }

        @Override
        public void save(JsonObject object) {
            object.addProperty("name", varName);
            object.add("val", method.toJson());
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            map.getVar(varName).setValue(method.call(map));
        }
    }
}
