package net.kapitencraft.scripted.code.exe.methods.param;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.edit.client.RenderMap;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class ParamData extends RenderMap {
    private final List<Method<?>.Instance> params = new ArrayList<>();
    private final ParamSet.Entry entry;

    public ParamData(ParamSet.Entry entry) {
        this.entry = entry;
    }

    public static ParamData create(ParamSet set, List<Method<?>.Instance> methods, VarAnalyser analyser) {
        ParamData data = new ParamData(set.getEntryForArgMethods(methods, analyser));
        methods.forEach(data::addParam);
        return data;
    }

    public static ParamData of(JsonObject parent, VarAnalyser analyser, ParamSet set) {
        JsonArray params = GsonHelper.getAsJsonArray(parent, "params");
        List<Method<?>.Instance> list = new ArrayList<>();
        params.asList().stream()
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .map(object -> Method.loadInstance(object, analyser))
                .forEach(list::add);
        return create(set, list, analyser);
    }

    public static ParamData empty() {
        return new ParamData(ParamSet.builder());
    }

    public static ParamData create(List<? extends Method<?>.Instance> list, VarAnalyser analyser, ParamSet set) {
        ParamSet.Entry entry = set.getEntryForArgMethods((List<Method<?>.Instance>) list, analyser);
        ParamData data = new ParamData(entry);
        data.params.addAll(list);
        return data;
    }


    public void addParam(Method<?>.Instance method) {
        params.add(method);
    }

    public JsonArray toJson() {
        JsonArray array = new JsonArray();
        this.params.stream().map(Method.Instance::toJson)
                .forEach(array::add);
        return array;
    }

    public boolean isEmpty() {
        return params.isEmpty();
    }

    public List<Method<?>.Instance> getParams() {
        return params;
    }

    public VarMap apply(VarMap parent) {
        return entry.apply(this, parent);
    }
}