package net.kapitencraft.scripted.code.exe.methods.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.kap_lib.collection.MapStream;
import net.kapitencraft.kap_lib.helpers.IOHelper;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.util.JsonHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SwitchMethod<T> extends Method<T> {
    private static Map<VarType<?>, SwitchMethod<?>> applied = new HashMap<>();

    public static <T> SwitchMethod<T> getOrCreate(VarType<T> type) {
        return (SwitchMethod<T>) applied.computeIfAbsent(type, SwitchMethod::new);
    }

    private final VarType<T> targetType;

    protected SwitchMethod(VarType<T> targetType) {
        super(set -> set.addEntry(entry -> entry.addParam("key", ()-> targetType)), "switch");
        this.targetType = targetType;
    }

    @Override
    public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return null;
    }

    //TODO fix

    private <I> Method<T>.Instance readInstance(JsonObject object, VarAnalyser analyser, ParamData data) {
        PrimitiveType<I> instanceType = (PrimitiveType<I>) JsonHelper.readType(object, "instanceType");
        Method<T>.Instance defaulted = Method.loadInstance(object, analyser);
        return new Instance<>(data, defaulted, instanceType, null);
    }

    private class Instance<I> extends Method<T>.Instance {
        private final HashMap<I, Method<T>.Instance> content = new HashMap<>();
        private final Method<T>.Instance defaulted;
        private final PrimitiveType<I> instanceType;

        private Instance(ParamData data, Method<T>.Instance defaulted, PrimitiveType<I> instanceType, HashMap<I, Method<T>.Instance> content) {
            super(data);
            this.defaulted = defaulted;
            this.instanceType = instanceType;
            this.content.putAll(content);
        }

        @Override
        protected T call(VarMap params, VarMap origin) {
            return content.getOrDefault(params.getVarValue("key", ()-> instanceType), defaulted).callInit(origin);
        }

        @Override
        public VarType<T> getType(IVarAnalyser analyser) {
            return targetType;
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.addProperty("instanceType", JsonHelper.saveType(instanceType));
            super.saveAdditional(object);
        }
    }
}