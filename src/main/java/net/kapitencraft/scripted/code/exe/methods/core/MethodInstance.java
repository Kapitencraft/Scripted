package net.kapitencraft.scripted.code.exe.methods.core;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

public abstract class MethodInstance<T> {

    public abstract T call(VarMap origin, MethodPipeline<?> pipeline);

    public VarType<?>.InstanceMethod<?>.Instance loadChild(JsonObject then, VarAnalyser analyser) {
        return this.getType(analyser).buildMethod(then, analyser, this);
    }

    public abstract VarType<T> getType(IVarAnalyser analyser);

    public Var<T> buildVar(VarMap origin, MethodPipeline<?> pipeline) {
        return new Var<>(this.getType(origin), this.call(origin, pipeline), true);
    }

    public boolean matchesType(VarAnalyser analyser, MethodInstance<?> other) {
        VarType<T> type = this.getType(analyser);
        VarType<?> otherType = other.getType(analyser);
        return type.matches(otherType);
    }

    //save
    /**
     * use to add more information to the save.
     * @param object the data storage
     */
    protected void saveAdditional(JsonObject object) {}

    //TODO save parent and ID
    public final JsonObject toJson() {
        JsonObject object = new JsonObject();
        saveAdditional(object);
        return object;
    }
}
