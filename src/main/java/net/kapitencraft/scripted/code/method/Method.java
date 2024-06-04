package net.kapitencraft.scripted.code.method;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.scripted.code.method.mapper.References;
import net.kapitencraft.scripted.code.method.param.ParamSet;
import net.kapitencraft.scripted.code.method.param.ParamData;
import net.kapitencraft.scripted.code.oop.InstanceMethod;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.function.Function;

public abstract class Method<T> {
    private final ParamSet set;
    private final String name;

    protected Method(ParamSet set, String name) {
        this.set = set;
        this.name = name;
    }


    public abstract Instance load(JsonObject object, VarAnalyser analyser, ParamData data);

    public abstract class Instance {
        private final ParamData paramData;

        protected Instance(ParamData paramData) {
            this.paramData = paramData;
        }

        @Override
        public String toString() {
            if (this.paramData.isEmpty()) return name;
            return name + "(" + paramData + ")";
        }

        public void analyse(VarAnalyser analyser) {}

        public Var<T> callInit(VarMap parent) {
            return callInit(this::call, parent);
        }

        protected Var<T> callInit(Function<VarMap, Var<T>> callFunc, VarMap parent) {
            VarMap map = set.getEntryForData(paramData).apply(paramData, parent);
            return callFunc.apply(map);
        }

        protected abstract Var<T> call(VarMap params);

        public JsonObject toJson() {
            JsonObject object = new JsonObject();
            object.addProperty("name", name);
            if (!this.paramData.isEmpty()) object.add("params", this.paramData.toJson());
            return object;
        }

        public InstanceMethod<?, ?>.Instance loadChild(JsonObject then, VarAnalyser analyser) {
            return this.getType(analyser).buildMethod(then, analyser, this);
        }

        public abstract VarType<T> getType(VarAnalyser analyser);
    }

    public static <T> Method<T>.Instance loadInstance(JsonObject object, VarAnalyser analyser) {
        String name = GsonHelper.getAsString(object, "name");
        Method<?>.Instance method;
        if (name.contains(".")) { //reading constructors
            String[] id = name.split(".");
            VarType<?> type = ModRegistries.VAR_TYPES.getValue(new ResourceLocation(id[0]));
            if (type == null) throw new JsonSyntaxException("unknown constructor key: '" + id[0] + "'");
            method = type.buildConstructor(ParamData.of(object, analyser));
        } else {
            method = References.VAR.load(GsonHelper.getAsString(object, "name"));
        }

        if (object.has("then")) {
            return (Method<T>.Instance) method.loadChild(object.getAsJsonObject("then"), analyser);
        }
        return (Method<T>.Instance) method;
    }

    public static <T> Method<T>.Instance loadFromSubObject(JsonObject object, String name, VarAnalyser analyser) {
        return loadInstance(GsonHelper.getAsJsonObject(object, name), analyser);
    }
}