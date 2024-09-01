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

public class SwitchMethod {

    //TODO fix

    private <I, T> MethodInstance<T> readInstance(JsonObject object, VarAnalyser analyser) {
        PrimitiveType<I> instanceType = (PrimitiveType<I>) JsonHelper.readType(object, "instanceType");
        VarType<T> targetType = JsonHelper.readType(object, "targetType");
        MethodInstance<T> defaulted = Method.loadInstance(object, "default", analyser);
        MethodInstance<I> provider = Method.loadInstance(object, "provider", analyser);
        Map<I, MethodInstance<T>> content = IOHelper.readMap(
                GsonHelper.getAsJsonArray(object, "content"),
                (object1, string) -> instanceType.loadFromJson(object1.getAsJsonPrimitive(string)),
                (object1, string) -> (MethodInstance<T>) Method.loadInstance(object1, string, analyser)
        ).toMap();
        return new Instance<>(defaulted, instanceType, content, provider, targetType);
    }

    private static class Instance<I, T> extends MethodInstance<T> {
        private final HashMap<I, MethodInstance<T>> content = new HashMap<>();
        private final MethodInstance<I> provider;
        private final VarType<T> targetType;
        @NotNull
        private final MethodInstance<T> defaulted;
        private final PrimitiveType<I> instanceType;

        private Instance(@NotNull MethodInstance<T> defaulted, PrimitiveType<I> instanceType, Map<I, MethodInstance<T>> content, MethodInstance<I> provider, VarType<T> targetType) {
            super("switch");
            this.defaulted = defaulted;
            this.instanceType = instanceType;
            this.provider = provider;
            this.targetType = targetType;
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
            object.addProperty("targetType", JsonHelper.saveType(targetType));
            object.add("content", IOHelper.writeMap(content, instanceType::saveToJson, MethodInstance::toJson));
            super.saveAdditional(object);
        }
    }
}