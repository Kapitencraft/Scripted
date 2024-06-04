package net.kapitencraft.scripted.code.method.elements.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.method.Method;
import net.kapitencraft.scripted.code.method.MethodPipeline;
import net.kapitencraft.scripted.code.method.elements.abstracts.Function;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;

import javax.annotation.Nullable;

public class ReturnFunction extends Function {

    @Override
    public Function.Instance load(JsonObject object, VarAnalyser analyser) {
        if (object.has("value")) return new Instance(Method.loadFromSubObject(object, "value", analyser));
        else return new Instance(null);
    }

    public class Instance extends Function.Instance {
        private final @Nullable Method<?> value;

        public Instance(@Nullable Method<?> value) {
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
            else pipeline.cancel((T) this.value.call(map).getValue());
        }
    }
}
