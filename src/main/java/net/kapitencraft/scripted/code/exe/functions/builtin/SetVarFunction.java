package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.functions.abstracts.SpecialFunction;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.builtin.primitive.Comparators;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.edit.client.text.Compiler;
import net.minecraft.util.GsonHelper;

import java.util.regex.Matcher;

public class SetVarFunction extends SpecialFunction {

    @Override
    public Instance<?> load(JsonObject object, VarAnalyser analyser) {
        String name = GsonHelper.getAsString(object, "name");
        Method<?>.Instance method = Method.loadFromSubObject(object, "val", analyser);
        return new Instance<>(name, method);
    }

    public <T> SetVarFunction.Instance<T> create(String varName, Method<T>.Instance inst) {
        return new Instance<>(varName, inst);
    }

    @Override
    public Function.Instance create(String in, VarAnalyser analyser) {
        String varName = in.substring(0, in.indexOf('='));
        Method<?>.Instance instance = Compiler.compileMethodChain(in.substring(in.indexOf('=')), true, analyser, analyser.getType(varName));
        return new Instance<>(varName, instance);
    }

    @Override
    public boolean isInstance(String string) {
        Matcher matcher = Comparators.COMPARATORS.matcher(string);
        return string.contains("=") && (matcher.find() && matcher.start() > string.indexOf('='));
    }

    public class Instance<T> extends Function.Instance {
        protected final String varName;
        private final Method<T>.Instance method;

        public Instance(String varName, Method<T>.Instance method) {
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
            map.getVar(varName).setValue(method.callInit(map));
        }
    }
}
