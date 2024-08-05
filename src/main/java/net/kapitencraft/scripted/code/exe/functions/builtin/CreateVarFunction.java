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

import java.util.function.Consumer;

public class CreateVarFunction extends Function {

    protected CreateVarFunction(Consumer<ParamSet> paramBuilder) {
        super(paramBuilder, "%ignored");
    }

    public CreateVarFunction() {
        this(ParamSet.empty());
    }

    @Override
    public Instance load(JsonObject object, VarAnalyser analyser) {
        return load(object);
    }

    @Override
    public Method<Void>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return load(object);
    }

    private Instance load(JsonObject object) {
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
            super(ParamData.empty());
            this.name = name;
            this.type = type;
            this.isFinal = isFinal;
        }

        @Override
        public void saveAdditional(JsonObject object) {
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
