package net.kapitencraft.scripted.code.exe.param;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.edit.graphical.RenderMap;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class ParamData extends RenderMap {
    private final WildCardData wildCardData = new WildCardData();
    private final List<Method<?>.Instance> params = new ArrayList<>();
    private final ParamSet.Entry entry;

    public ParamData(ParamSet.Entry entry) {
        this.entry = entry;
    }

    public static ParamData empty() {
        return new ParamData(new ParamSet.Entry()); //type can be ignored
    }

    public static ParamData of(JsonObject parent, VarAnalyser analyser, ParamSet set) {
        List<Method<?>.Instance> list = new ArrayList<>();
        if (parent.has("params")) {
            JsonHelper.castToObjects(GsonHelper.getAsJsonArray(parent, "params"))
                    .map(object -> Method.loadInstance(object, analyser))
                    .forEach(list::add);
        }
        return create(set, analyser, list);
    }

    public static ParamData create(ParamSet set, VarAnalyser analyser, List<? extends Method<?>.Instance> list) {
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

    void applyWildCardType(String name, VarType<?> type) {
        this.wildCardData.applyType(name, type);
    }

    public boolean hasWildCard(String wildCardName) {
        return this.wildCardData.getType(wildCardName) != null;
    }

    public <T> VarType<T> getWildCardType(String wildCardName) {
        return this.wildCardData.getType(wildCardName);
    }
}