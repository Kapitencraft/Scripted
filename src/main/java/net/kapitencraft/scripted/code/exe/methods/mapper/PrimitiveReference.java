package net.kapitencraft.scripted.code.exe.methods.mapper;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.primitive.PrimitiveType;
import net.kapitencraft.scripted.init.ModMethods;
import net.kapitencraft.scripted.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;

public class PrimitiveReference<T> extends Method<T> {

    public PrimitiveReference() {
        super(ParamSet.empty(), "primitive"); //name ignored;
    }

    @Override
    public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        PrimitiveType<T> type = (PrimitiveType<T>) JsonHelper.readType(object, "type");
        T value = type.loadFromJson(object);
        return new Instance(value, type);
    }

    @Override
    protected Method<T>.Instance create(ParamData data, Method<?>.Instance parent) {
        return null;
    }

    public class Instance extends Method<T>.Instance {
        private final T value;
        private final PrimitiveType<T> type;

        private Instance(T value, PrimitiveType<T> type) {
            super(null);
            this.value = value;
            this.type = type;
        }

        @Override
        protected T call(VarMap params) {
            return value;
        }

        @Override
        public VarType<T> getType(IVarAnalyser analyser) {
            return type;
        }

        @Override
        public JsonObject toJson() {
            JsonObject object = new JsonObject();
            object.addProperty("type", JsonHelper.saveType(type));
            type.saveToJson(object, value);
            return object;
        }
    }

    public static <T> @Nullable PrimitiveReference<T>.Instance loadFromString(String string, PrimitiveType<T> type) {
        Matcher matcher = type.matcher().matcher(string);
        if (matcher.matches()) {
            return ModMethods.PRIMITIVE.get().create((PrimitiveType<T>) type, matcher.group(1));
        }
        return null;
    }

    private PrimitiveReference<T>.Instance create(PrimitiveType<T> type, String value) {
        return new Instance(type.loadPrimitive(value), type);
    }
}