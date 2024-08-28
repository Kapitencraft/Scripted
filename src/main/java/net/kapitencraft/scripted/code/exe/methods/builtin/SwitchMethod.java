package net.kapitencraft.scripted.code.exe.methods.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.kap_lib.helpers.IOHelper;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SwitchMethod<T> extends Method<T> {
    private static final Map<VarType<?>, SwitchMethod<?>> CACHE = new HashMap<>();

    public static <T> SwitchMethod<T> getOrCreate(VarType<T> type) {
        return (SwitchMethod<T>) CACHE.computeIfAbsent(type, SwitchMethod::new);
    }

    private final VarType<T> targetType;

    protected SwitchMethod(VarType<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public MethodInstance<T> load(JsonObject object, VarAnalyser analyser) {
        return readInstance(object, analyser);
    }

    //TODO fix

    private <I> MethodInstance<T> readInstance(JsonObject object, VarAnalyser analyser) {
        PrimitiveType<I> instanceType = (PrimitiveType<I>) JsonHelper.readType(object, "instanceType");
        MethodInstance<T> defaulted = Method.loadInstance(object, "default", analyser);
        MethodInstance<I> provider = Method.loadInstance(object, "provider", analyser);
        Map<I, MethodInstance<T>> content = IOHelper.readMap(
                GsonHelper.getAsJsonArray(object, "content"),
                (object1, string) -> instanceType.loadFromJson(object1.getAsJsonPrimitive(string)),
                (object1, string) -> (MethodInstance<T>) Method.loadInstance(object1, string, analyser)
        ).toMap();
        return new Instance<>(defaulted, instanceType, content, provider);
    }

    private class Instance<I> extends MethodInstance<T> {
        private final HashMap<I, MethodInstance<T>> content = new HashMap<>();
        private final MethodInstance<I> provider;
        @NotNull
        private final MethodInstance<T> defaulted;
        private final PrimitiveType<I> instanceType;

        private Instance(@NotNull MethodInstance<T> defaulted, PrimitiveType<I> instanceType, Map<I, MethodInstance<T>> content, MethodInstance<I> provider) {
            this.defaulted = defaulted;
            this.instanceType = instanceType;
            this.provider = provider;
            this.content.putAll(content);
        }

        @Override
        public T call(VarMap origin, MethodPipeline<?> pipeline) {
            return content.getOrDefault(provider.call(origin, pipeline), defaulted).call(origin, pipeline);
        }

        @Override
        public VarType<T> getType(IVarAnalyser analyser) {
            return targetType;
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add("default", defaulted.toJson());
            object.add("provider", provider.toJson());
            object.addProperty("instanceType", JsonHelper.saveType(instanceType));
            object.add("content", IOHelper.writeMap(content, instanceType::saveToJson, MethodInstance::toJson));
            super.saveAdditional(object);
        }
    }
}