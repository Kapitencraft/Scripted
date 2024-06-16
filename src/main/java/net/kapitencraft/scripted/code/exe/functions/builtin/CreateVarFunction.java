package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.functions.abstracts.SpecialFunction;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.edit.client.text.Compiler;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;

public class CreateVarFunction extends SpecialFunction {

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

    @Override
    public Function.Instance create(String in, VarAnalyser analyser) {
        boolean isFinal = in.startsWith("final ");
        if (isFinal) in = in.substring(6); //remove 'final' so it doesn't interfere with the name and Type calculation
        VarType<?> type = Compiler.readType(in.substring(0, in.indexOf(' ')));
        String name = in.substring(in.indexOf(' '));
        return new Instance(name, type, isFinal);
    }

    @Override
    public boolean isInstance(String string) {
        return !string.contains("(") && !string.contains(")") && string.split(" ").length == 2; //wanky way to check for the amount of chars in a string ig
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
