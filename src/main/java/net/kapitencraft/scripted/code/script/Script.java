package net.kapitencraft.scripted.code.script;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.script.type.ScriptType;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;

import java.util.List;

public class Script<T> {
    private final List<String> usedParams;
    private final ScriptType type;
    private final MethodPipeline<T> code;

    public Script(List<String> usedParams, ScriptType type, MethodPipeline<T> code) {
        this.usedParams = usedParams;
        this.type = type;
        this.code = code;
    }

    public Var<T> execute(VarMap map) {
        return code.execute(map, null);
    }

    public JsonElement save() {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        usedParams.forEach(array::add);
        object.add("params", array);
        object.add("code", code.toJson());
        return object;
    }
}
