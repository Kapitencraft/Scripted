package net.kapitencraft.scripted.code.exe;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

public abstract class Runnable {
    protected final ParamSet paramSet;

    protected Runnable(ParamSet paramSet) {
        this.paramSet = paramSet;
    }

    public abstract Instance load(JsonObject object, VarAnalyser analyser);

    public static abstract class Instance {
        private final ParamData paramData;

        protected Instance(ParamData paramData) {
        this.paramData = paramData;
    }

        protected abstract void execute(VarMap applied, MethodPipeline<?> pipeline);

        public void invoke(VarMap parent, MethodPipeline<?> pipeline) {
        this.execute(this.apply(parent), pipeline);
    }

        private VarMap apply(VarMap parent) {
        return paramData.apply(parent);
    }

        public void analyse(VarAnalyser analyser) {}

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
}