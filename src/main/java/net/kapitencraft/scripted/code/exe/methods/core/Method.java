package net.kapitencraft.scripted.code.exe.methods.core;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public abstract class Method<T> {

    public abstract MethodInstance<T> load(JsonObject object, VarAnalyser analyser);

    public static <T> MethodInstance<T> loadInstance(JsonObject object, String name, VarAnalyser analyser) {
        return loadInstance(GsonHelper.getAsJsonObject(object, name), analyser);
    }

    public static <T> MethodInstance<T> loadInstance(JsonObject object, VarAnalyser analyser) {
        String type = GsonHelper.getAsString(object, "type");
        MethodInstance<?> inst;
        if (type.startsWith("new")) {
            VarType<?> varType = VarType.NAME_MAP.get(type.substring(3));
            inst = varType.buildConstructor(object, analyser);
        } else if (type.contains("-")) {
            VarType<?> varType = VarType.NAME_MAP.get(type.substring(type.indexOf('-')));
            String methodType = type.substring(0, type.indexOf('-'));
            inst = switch (methodType) {
                case "Comp":
                    yield varType.loadComparator(object, analyser);
                case "Math":
                    yield varType.loadMathOperation(object, analyser);
                case "When":
                    yield varType.loadWhen(object, analyser);
                case "SetVar":
                    yield varType.loadSetVar(object, analyser);
                default:
                    throw new IllegalStateException("Unexpected value: " + methodType);
            };
        } else if (type.startsWith("primitive$")) {
            PrimitiveType<?> prim = (PrimitiveType<?>) VarType.NAME_MAP.get(type.substring(10));
            inst = prim.loadInstance(object);
        } else {
            Method<?> method = ModRegistries.METHODS.getValue(new ResourceLocation(type)); //1.21 update; i'm a cry
            if (method == null) throw new IllegalStateException("couldn't find method called '" + type + "'");
            inst = method.load(object, analyser);
        }
        return (MethodInstance<T>) inst;
    }

    public static <T> MethodInstance<T> loadFromSubObject(JsonObject object, String name, VarAnalyser analyser) {
        return loadInstance(GsonHelper.getAsJsonObject(object, name), analyser);
    }
}