package net.kapitencraft.scripted.code.exe.methods;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.ModMethods;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class Method<T> {
    protected final String name;
    protected final ParamSet paramSet;

    protected Method(Consumer<ParamSet> setBuilder, String name) {
        this.name = name;
        this.paramSet = build(setBuilder);
    }

    public ParamSet set() {
        return paramSet;
    }

    private static ParamSet build(Consumer<ParamSet> builder) {
        ParamSet set = new ParamSet();
        builder.accept(set);
        return set;
    }

    public String name() {
        return name;
    }

    public Instance load(JsonObject object, VarAnalyser analyser) {
        return load(object, analyser, ParamData.of(object, analyser, this.paramSet));
    }

    public abstract Instance load(JsonObject object, VarAnalyser analyser, ParamData data);

    public abstract class Instance {
        protected final ParamData paramData;

        protected Instance(ParamData paramData) {
            this.paramData = paramData;
        }

        @Override
        public String toString() {
            if (this.paramData.isEmpty()) return name();
            return name() + "(" + paramData + ")";
        }

        public void analyse(VarAnalyser analyser) {
            paramSet.analyse(analyser, paramData);
        }

        public T callInit(VarMap parent) {
            return callInit(this::call, parent);
        }

        public Var<T> buildVar(VarMap parent) {
            return new Var<>(this.getType(parent), callInit(parent), true);
        }

        public void execute(VarMap map, MethodPipeline<?> pipeline) {
            callInit(map);
        }

        protected T callInit(BiFunction<VarMap, VarMap, T> callFunc, VarMap parent) {
            VarMap map = paramData.apply(parent);
            return callFunc.apply(map, parent);
        }

        protected abstract T call(VarMap params, VarMap origin);

        public VarType<?>.InstanceMethod<?>.Instance loadChild(JsonObject then, VarAnalyser analyser) {
            return this.getType(analyser).buildMethod(then, analyser, this);
        }

        public abstract VarType<T> getType(IVarAnalyser analyser);

        public boolean matchesType(VarAnalyser analyser, Method<?>.Instance other) {
            VarType<T> type = this.getType(analyser);
            VarType<?> otherType = other.getType(analyser);
            return type.matches(otherType);
        }

        public void invoke(VarMap parent, MethodPipeline<?> pipeline) {
            this.execute(this.apply(parent), pipeline);
        }

        private VarMap apply(VarMap parent) {
            return paramData.apply(parent);
        }

        //save
        /**
         * use to add more information to the save. <br>
         * do not use
         * <blockquote><pre>
         *     "params", "type"
         * </pre></blockquote>
         * because they've already been used
         *
         * @param object the data storage
         */
        protected void saveAdditional(JsonObject object) {}

        public final JsonObject toJson() {
            JsonObject object = new JsonObject();
            object.add("params", this.paramData.toJson());
            saveAdditional(object);
            return object;

        }
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