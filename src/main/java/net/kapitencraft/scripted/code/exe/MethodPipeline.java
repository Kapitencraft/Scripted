package net.kapitencraft.scripted.code.exe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class MethodPipeline<T> {
    private final VarType<T> returnType;
    private final List<Function.Instance> functions;
    private final boolean isLoop;
    private boolean canceled, broken, continued, stopped;
    private T ret;

    public MethodPipeline(VarType<T> returnType, List<Function.Instance> functions, boolean isLoop) {
        this.returnType = returnType;
        this.functions = functions;
        this.isLoop = isLoop;
    }

    /**
     * @param object the {@link JsonObject} containing the data
     * @param analyser a VarAnalyser to load internal methods
     * @param isLoop whether the returned Pipeline should allow {@link MethodPipeline#setBroken() breaks} and {@link MethodPipeline#setContinued() continues}
     * @param <T> the return type of the Pipeline
     * @return the decompiled Pipeline; may throw an exception when anything fails
     */
    public static <T> MethodPipeline<T> load(JsonObject object, VarAnalyser analyser, boolean isLoop) {
        VarType<T> type = JsonHelper.readType(object, "return");
        List<Function.Instance> list = readInstances(object, analyser);
        list.forEach(instance -> {
            analyser.next();
            instance.analyse(analyser);
        }); //analyse
        return new MethodPipeline<>(type, list, isLoop);
    }

    private static List<Function.Instance> readInstances(JsonObject object, VarAnalyser analyser) {
        JsonArray array = GsonHelper.getAsJsonArray(object, "func");
        return array.asList().stream().filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .map(object1 -> JsonHelper.readFunction(object1, analyser))
                .toList();
    }

    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("return", JsonHelper.saveType(this.returnType));
        JsonArray array = new JsonArray();
        this.functions.stream().map(JsonHelper::saveFunction).forEach(array::add);
        object.add("func", array);
        return object;
    }

    public Var<T> execute(VarMap map, @Nullable MethodPipeline<T> source) {
        map.push();
        Iterator<Function.Instance> iterator = this.functions.iterator();
        while (iterator.hasNext() && !stopped) {
            iterator.next().execute(map, this);
        }
        map.pop();
        if (stopped) {
            if (source != null) {
                if (canceled) {
                    source.cancel(this.ret);
                } else if (broken && !isLoop) {
                    source.setBroken();
                } else if (continued && !isLoop) {
                    source.setContinued();
                }
            } else {
                if ((broken || continued) && !isLoop) {
                    throw new IllegalStateException("can not break nor continue not-loop pipeline");
                } else {
                    return new Var<>(returnType, this.ret);
                }
            }
        }
        return new Var<>(returnType);
    }

    public void setCanceled() {
        this.canceled = true;
        this.stopped = true;
    }

    public void cancel(T value) {
        this.setCanceled();
        this.ret = value;
    }

    public void setBroken() {
        this.broken = true;
        this.stopped = true;
    }

    public void setContinued() {
        this.continued = true;
        this.stopped = true;
    }

    public boolean isBrokenOrCanceled() {
        return stopped && !continued;
    }

    public void analyse(VarAnalyser analyser) {
        analyser.push();
        this.functions.forEach(instance -> {
            analyser.next();
            instance.analyse(analyser);
        });
        analyser.pop();
    }
}
