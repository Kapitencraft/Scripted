package net.kapitencraft.scripted.code.method.elements.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.method.MethodPipeline;
import net.kapitencraft.scripted.code.method.elements.abstracts.Function;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;

public class CreateVarFunction extends Function {

    @Override
    public Instance load(JsonObject object, VarAnalyser analyser) {
        String name = GsonHelper.getAsString(object, "name");
        VarType<?> varType = JsonHelper.readType(object, "var_type");
        return new Instance(name, varType);
    }

    public class Instance extends Function.Instance {
        private final String name;
        private final VarType<?> type;

        public Instance(String name, VarType<?> type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public void save(JsonObject object) {
            object.addProperty("name", name);
            object.addProperty("var_type", JsonHelper.saveType(this.type));
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            map.addVarType(name, type);
        }
    }
}
