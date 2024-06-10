package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

import javax.annotation.Nullable;

public class ReturnFunction extends Function {

    @Override
    public Function.Instance load(JsonObject object, VarAnalyser analyser) {
        if (object.has("value")) return new Instance(Method.loadFromSubObject(object, "value", analyser));
        else return new Instance(null);
    }

    public class Instance extends Function.Instance {
        private final @Nullable Method<?>.Instance value;

        public Instance(@Nullable Method<?>.Instance value) {
            this.value = value;
        }

        @Override
        public void save(JsonObject object) {
            if (value != null) {
                object.add("value", value.toJson());
            }
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            cancelPipeline(map, source);
        }

        private <T> void cancelPipeline(VarMap map, MethodPipeline<T> pipeline) {
            if (this.value == null) pipeline.setCanceled();
            else pipeline.cancel((T) this.value.callInit(map).getValue());
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            analyser.setCanceled();
        }
    }
}
