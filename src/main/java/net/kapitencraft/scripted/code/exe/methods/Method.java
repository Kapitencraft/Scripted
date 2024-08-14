package net.kapitencraft.scripted.code.exe.methods;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.ModMethods;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public abstract class Method<T> {

    public abstract Instance load(JsonObject object, VarAnalyser analyser);

    public abstract class Instance {

        public abstract T call(VarMap origin, MethodPipeline<?> pipeline);

        public VarType<?>.InstanceMethod<?>.Instance loadChild(JsonObject then, VarAnalyser analyser) {
            return this.getType(analyser).buildMethod(then, analyser, this);
        }

        public abstract VarType<T> getType(IVarAnalyser analyser);

        public Var<T> buildVar(VarMap origin, MethodPipeline<?> pipeline) {
            return new Var<>(this.getType(origin), this.call(origin, pipeline), true);
        }

        public boolean matchesType(VarAnalyser analyser, Method<?>.Instance other) {
            VarType<T> type = this.getType(analyser);
            VarType<?> otherType = other.getType(analyser);
            return type.matches(otherType);
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