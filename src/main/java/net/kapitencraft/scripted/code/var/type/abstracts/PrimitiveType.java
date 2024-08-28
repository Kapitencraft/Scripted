package net.kapitencraft.scripted.code.var.type.abstracts;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.init.custom.ModCallbacks;
import net.kapitencraft.scripted.init.custom.ModRegistries;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

public abstract class PrimitiveType<I> extends VarType<I> {
    public static final List<PrimitiveType<?>> PRIMITIVES = ModRegistries.VAR_TYPES.getSlaveMap(ModCallbacks.VarTypes.PRIMITIVES, List.class);

    public PrimitiveType(String name, BiFunction<I, I, I> add, BiFunction<I, I, I> mult, BiFunction<I, I, I> div, BiFunction<I, I, I> sub, BiFunction<I, I, I> mod, Comparator<I> comp) {
        super(name, add, mult, div, sub, mod, comp);
    }

    public abstract I loadPrimitive(String string);

    public abstract JsonPrimitive saveToJson(I value);

    public abstract I loadFromJson(JsonPrimitive object);

    public MethodInstance<I> readPrimitiveInstance(String string) {
        return new Reference(loadPrimitive(string));
    }

    public MethodInstance<I> loadInstance(JsonObject object) {
        return new Reference(loadFromJson(object.getAsJsonPrimitive("value")));
    }

    private class Reference extends MethodInstance<I> {
        private final I value;

        private Reference(I value) {
            this.value = value;
        }

        @Override
        public I call(VarMap origin, MethodPipeline<?> pipeline) {
            return value;
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add("value", PrimitiveType.this.saveToJson(value));
        }

        @Override
        public VarType<I> getType(IVarAnalyser analyser) {
            return PrimitiveType.this;
        }
    }
}