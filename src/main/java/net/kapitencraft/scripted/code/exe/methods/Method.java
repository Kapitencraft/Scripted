package net.kapitencraft.scripted.code.exe.methods;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.scripted.code.exe.IExecutable;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.edit.client.IRenderable;
import net.kapitencraft.scripted.edit.client.RenderMap;
import net.kapitencraft.scripted.edit.client.text.Compiler;
import net.kapitencraft.scripted.init.ModMethods;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Method<T> {
    protected final ParamSet set;
    private final String name;

    protected Method(Consumer<ParamSet> setBuilder, String name) {
        this.set = ParamSet.method();
        setBuilder.accept(this.set);
        this.name = name;
    }

    public abstract Instance load(JsonObject object, VarAnalyser analyser, ParamData data);

    protected abstract Instance create(ParamData data, Method<?>.Instance parent);

    public Method<?>.Instance readFromCode(String args, VarAnalyser analyser, Method<?>.Instance parent) {
        if (args == null) {
            if (this.set.isEmpty()) {
                return create(ParamData.empty(), parent);
            }
            return null;
        } else {
            String[] split = args.split(",");
            List<? extends Method<?>.Instance> list = Arrays.stream(split).map(s -> Compiler.compileMethodChain(s, true, analyser)).toList();
            return create(ParamData.create(list, analyser, set), parent);
        }
    }

    public abstract class Instance implements IExecutable {
        protected final ParamData paramData;

        protected Instance(ParamData paramData) {
            this.paramData = paramData;
        }

        @Override
        public String toString() {
            if (this.paramData.isEmpty()) return name;
            return name + "(" + paramData + ")";
        }

        public void analyse(VarAnalyser analyser) {
            set.analyse(analyser, paramData);
        }

        public T callInit(VarMap parent) {
            return callInit(this::call, parent);
        }

        public Var<T> buildVar(VarMap parent) {
            return new Var<>(this.getType(parent), callInit(parent), true);
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> pipeline) {
            callInit(map);
        }

        protected T callInit(Function<VarMap, T> callFunc, VarMap parent) {
            VarMap map = paramData.apply(parent);
            return callFunc.apply(map);
        }

        protected abstract T call(VarMap params);

        public JsonObject toJson() {
            JsonObject object = new JsonObject();
            object.addProperty("name", name);
            if (!this.paramData.isEmpty()) object.add("params", this.paramData.toJson());
            return object;
        }

        public VarType<?>.InstanceMethod<?>.Instance loadChild(JsonObject then, VarAnalyser analyser) {
            return this.getType(analyser).buildMethod(then, analyser, this);
        }

        public abstract VarType<T> getType(IVarAnalyser analyser);

        public boolean matchesType(VarAnalyser analyser, Method<?>.Instance other) {
            VarType<T> type = this.getType(analyser);
            VarType<?> otherType = other.getType(analyser);
            return type.matches(otherType);
        }
    }

    public abstract class Builder implements IRenderable {

        @Override
        public boolean allowMethodRendering() {
            return false;
        }

        @Override
        public String translationKey() {
            return Util.makeDescriptionId("method", ModRegistries.METHODS.getKey(Method.this));
        }

        @Override
        public RenderMap getParamData() {
            return null;
        }

        public abstract Method<T>.Instance build();
    }

    public static <T> Method<T>.Instance loadInstance(JsonObject object, VarAnalyser analyser) {
        String name = GsonHelper.getAsString(object, "name");
        Method<?>.Instance method;
        if (name.contains(".")) { //reading constructors
            String[] id = name.split("\\.");
            VarType<?> type = ModRegistries.VAR_TYPES.getValue(new ResourceLocation(id[0]));
            if (type == null) throw new JsonSyntaxException("unknown constructor key: '" + id[0] + "'");
            method = type.buildConstructor(object, analyser);
        } else {
            method = ModMethods.VAR_REFERENCE.get().load(GsonHelper.getAsString(object, "name"));
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