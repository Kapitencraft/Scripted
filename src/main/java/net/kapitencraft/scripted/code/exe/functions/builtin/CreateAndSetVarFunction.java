package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.edit.client.text.Compiler;
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
        return create(name, method, type, false);
    }

    public <T> Instance<T> create(String name, Method<T>.Instance creator, VarType<T> type, boolean isFinal) {
        return new Instance<>(name, creator, type, isFinal);
    }

    @Override
    public Function.Instance create(String in, VarAnalyser analyser) {
        return createInstance(in, analyser);
    }

    @Override
    public boolean isInstance(String string) {
        return super.isInstance(string) && string.indexOf(' ') < string.indexOf('=');
    }

    private <T> Instance<T> createInstance(String in, VarAnalyser analyser) {
        boolean isFinal = in.startsWith("final ");
        if (isFinal) in = in.substring(6); //remove 'final' so it doesn't interfere with the name and Type calculation
        VarType<T> type = Compiler.readType(in.substring(0, in.indexOf(' ')));
        String name = in.substring(in.indexOf(' '), in.indexOf('='));
        Method<T>.Instance sup = Compiler.compileMethodChain(in.substring(in.indexOf('=')), true, analyser, type);
        return new Instance<>(name, sup, type, isFinal);
    }

    public class Instance<T> extends SetVarFunction.Instance<T> {
        private final VarType<T> type;
        private final boolean isFinal;

        public Instance(String varName, Method<T>.Instance method, VarType<T> type, boolean isFinal) {
            super(varName, method);
            this.type = type;
            this.isFinal = isFinal;
        }

        @Override
        public void save(JsonObject object) {
            super.save(object);
            object.addProperty("var_type", JsonHelper.saveType(type));
            object.addProperty("isFinal", isFinal);
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            map.addVarType(varName, type, isFinal);
            super.execute(map, source);
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            analyser.registerVar(varName, type);
        }
    }
}