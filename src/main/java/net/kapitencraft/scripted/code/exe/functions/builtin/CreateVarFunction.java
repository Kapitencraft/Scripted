package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
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
        boolean isFinal = GsonHelper.getAsBoolean(object, "isFinal");
        return create(name, varType, isFinal);
    }

    public Instance create(String name, VarType<?> type, boolean isFinal) {
        return new Instance(name, type, isFinal);
    }

    public class Instance extends Function.Instance {
        private final String name;
        private final VarType<?> type;
        private final boolean isFinal;

        public Instance(String name, VarType<?> type, boolean isFinal) {
            this.name = name;
            this.type = type;
            this.isFinal = isFinal;
        }

        @Override
        public void save(JsonObject object) {
            object.addProperty("name", name);
            object.addProperty("var_type", JsonHelper.saveType(this.type));
            object.addProperty("isFinal", isFinal);
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            map.addVarType(name, type, isFinal);
        }
    }
}
