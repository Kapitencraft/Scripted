package net.kapitencraft.scripted.code.exe.methods.core;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

public abstract class MethodInstance<T> {
    private final String id;

    protected MethodInstance(String id) {
        this.id = id;
    }

    public abstract T call(VarMap origin, MethodPipeline<?> pipeline);

    public abstract VarType<T> getType(IVarAnalyser analyser);

    public Var<T> buildVar(VarMap origin, MethodPipeline<?> pipeline) {
        return new Var<>(this.getType(origin), this.call(origin, pipeline), true);
    }

    //save
    /**
     * use to add more information to the save.
     * @param object the data storage
     */
    protected void saveAdditional(JsonObject object) {}

    //TODO save ID
    public final JsonObject toJson() {
        JsonObject object = new JsonObject();
        saveAdditional(object);
        object.addProperty("type", id);
        return object;
    }
}
