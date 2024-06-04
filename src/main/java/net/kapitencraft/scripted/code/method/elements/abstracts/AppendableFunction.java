package net.kapitencraft.scripted.code.method.elements.abstracts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class AppendableFunction<T extends AppendableFunction<T>.AppendableInstance> extends Function {

    @Override
    public final Instance load(JsonObject object, VarAnalyser analyser) {
        AppendableInstance inst = loadInstance(object, analyser);
        if (object.has("appendings")) {
            JsonArray array = GsonHelper.getAsJsonArray(object, "appendings");
            List<Function.Instance> functions = array.asList().stream().filter(JsonElement::isJsonObject)
                    .map(JsonElement::getAsJsonObject)
                    .map(obj -> JsonHelper.readFunction(obj, analyser))
                    .toList();
            functions.forEach(instance -> {
                try {
                    AppendFunction<T>.AppendInstance appendInstance = (AppendFunction<T>.AppendInstance) instance;
                    inst.append(appendInstance);
                } catch (Exception e) {
                    throw new IllegalArgumentException("error loading appending element: " + e.getMessage());
                }
            });
        }
        return inst;
    }

    public abstract AppendableInstance loadInstance(JsonObject object, VarAnalyser analyser);

    public abstract class AppendableInstance extends Function.Instance {
        protected final List<AppendFunction<T>.AppendInstance> appendings = new ArrayList<>();

        protected AppendableInstance() {
        }

        public boolean append(AppendFunction<T>.AppendInstance func) {
            return appendings.add(func);
        }

        public boolean has(AppendFunction<T>.AppendInstance instance) {
            return appendings.stream().anyMatch(inst -> inst.getClass() == instance.getClass());
        }
    }
}
