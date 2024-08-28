package net.kapitencraft.scripted.code.exe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MethodPipeline<T> {
    private final VarType<T> returnType;
    private final List<MethodInstance<?>> functions;
    private final boolean isLoop;
    private boolean canceled, broken, continued, stopped;
    private VarMap map;
    private T ret;

    public MethodPipeline(VarType<T> returnType, List<MethodInstance<?>> functions, boolean isLoop) {
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
        List<MethodInstance<?>> list = readInstances(object, analyser);
        list.forEach(instance -> {
            analyser.next();
            //instance.analyse(analyser);
        }); //analyse
        return new MethodPipeline<>(type, list, isLoop);
    }

    private static List<MethodInstance<?>> readInstances(JsonObject object, VarAnalyser analyser) {
        JsonArray array = GsonHelper.getAsJsonArray(object, "func");
        List<MethodInstance<?>> list = new ArrayList<>();
        JsonHelper.castToObjects(array)
                .map(object1 -> Method.loadInstance(object1, analyser))
                .forEach(list::add);
        return list;
    }

    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("return", JsonHelper.saveType(this.returnType));
        JsonArray array = new JsonArray();
        this.functions.stream().map(MethodInstance::toJson).forEach(array::add);
        object.add("func", array);
        return object;
    }

    public Var<T> execute(VarMap map, @Nullable MethodPipeline<T> source) {
        this.map = map;
        map.push();
        Iterator<MethodInstance<?>> iterator = this.functions.iterator();
        while (iterator.hasNext() && !stopped) {
            iterator.next().call(map, this);
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
                } else if (canceled) {
                    this.reset();
                    return new Var<>(returnType, this.ret, true);
                }
            }
        }
        this.reset();
        if (this.returnType != VarTypes.VOID.get()) throw new IllegalStateException("missing return statement");
        return new Var<>(returnType, true);
    }

    public VarMap getMap() {
        return map;
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

    public void reset() {
        this.stopped = false;
        this.continued = false;
        this.canceled = false;
        this.broken = false;
        this.ret = null;
    }

    public boolean isBrokenOrCanceled() {
        return stopped && !continued;
    }

    public void analyse(VarAnalyser analyser) {
        analyser.push();
        this.functions.forEach(instance -> {
            analyser.next();
            //instance.analyse(analyser);
        });
        analyser.pop();
    }
}
