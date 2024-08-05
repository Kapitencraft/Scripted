package net.kapitencraft.scripted.code.oop;

import com.google.gson.JsonObject;
import net.kapitencraft.kap_lib.helpers.CollectorHelper;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
public class MethodMap<T> {
    private final HashMap<String, VarType<T>.InstanceMethod<?>> builders = new HashMap<>();
    private final List<Supplier<VarType<T>.InstanceMethod<?>>> generators = new ArrayList<>();

    public VarType<?>.InstanceMethod<?>.Instance buildMethod(JsonObject object, VarAnalyser analyser, Method<T>.Instance parent) {
        VarType<T>.InstanceMethod<?> method = builders.get(GsonHelper.getAsString(object, "type"));
        ParamData data = ParamData.of(object, analyser, method.set());
        VarType<T>.InstanceMethod<?>.Instance instance = method.load(data, analyser, parent, object);
        if (object.has("then")) return instance.loadChild(object.getAsJsonObject("then"), analyser);
        return instance;
    }

    public void registerMethod(Supplier<VarType<T>.InstanceMethod<?>> builder) {
        this.generators.add(builder);
    }

    @ApiStatus.Internal
    public void generate() {
        this.builders.putAll(this.generators
                        .stream()
                        .map(Supplier::get)
                .collect(CollectorHelper.createMapForKeys(VarType.InstanceMethod::name)));
    }

    public VarType<T>.InstanceMethod<?> getOrThrow(String name) {
        return Objects.requireNonNull(builders.get(name), "unknown method '" + name + "'");
    }
}
