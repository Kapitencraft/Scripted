package net.kapitencraft.scripted.code.exe.methods.mapper;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.ModMethods;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrimitiveReference<T> extends Method<T> {
    public static final Pattern STRING = Pattern.compile("\"(.+)\"");
    public static final Pattern CHAR = Pattern.compile("'(.)'");
    public static final Pattern NUMBER = Pattern.compile("(\\d+)");

    public PrimitiveReference() {
        super(ParamSet.empty(), "primitive"); //name ignored;
    }

    @Override
    public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        Type type = Type.CODEC.byName(GsonHelper.getAsString(object, "type"), Type.STRING);
        T value = type.getValue(object);
        return new Instance(value, type);
    }

    public Method<String>.Instance string(String s) {
        return new PrimitiveReference<String>.Instance(s, Type.STRING);
    }

    public Method<Character>.Instance character(String c) {
        return new PrimitiveReference<Character>.Instance(c.charAt(0), Type.CHAR);
    }

    public Method<Integer>.Instance integer(String i) {
        return new PrimitiveReference<Integer>.Instance(Integer.valueOf(i), Type.INTEGER);
    }

    public Method<Double>.Instance makeDouble(String d) {
        return new PrimitiveReference<Double>.Instance(Double.valueOf(d), Type.DOUBLE);
    }

    public class Instance extends Method<T>.Instance {
        private final T value;
        private final Type type;

        private Instance(T value, Type type) {
            super(null);
            this.value = value;
            this.type = type;
        }

        @Override
        protected Var<T> call(VarMap params) {
            return new Var<>((VarType<T>) type.getType(), value);
        }

        @Override
        public VarType<T> getType(VarAnalyser analyser) {
            return (VarType<T>) type.getType();
        }

        @Override
        public JsonObject toJson() {
            JsonObject object = new JsonObject();
            object.addProperty("type", type.getSerializedName());
            type.saveValue(object, value);
            return object;
        }
    }

    private enum Type implements StringRepresentable {
        STRING(PrimitiveReference.STRING, ModVarTypes.STRING, "string", GsonHelper::getAsString, String::toString, JsonObject::addProperty),
        CHAR(PrimitiveReference.CHAR, ModVarTypes.CHAR, "char", GsonHelper::getAsCharacter, s -> s.charAt(0), JsonObject::addProperty),
        INTEGER(PrimitiveReference.NUMBER, ModVarTypes.INTEGER, "integer", GsonHelper::getAsInt, Integer::valueOf, JsonObject::addProperty),
        DOUBLE(PrimitiveReference.NUMBER, ModVarTypes.DOUBLE, "double", GsonHelper::getAsDouble, Double::valueOf, JsonObject::addProperty);

        private final Supplier<? extends VarType<?>> typeSupplier;
        private final Pattern pattern;
        private final String name;
        private final BiFunction<JsonObject, String, ?> loader;
        private final Function<String, ?> mapper;
        private final TriConsumer<JsonObject, String, ?> saver;

        public static final EnumCodec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        <T> Type(Pattern pattern, Supplier<? extends VarType<T>> typeSupplier, String name, BiFunction<JsonObject, String, T> loader, Function<String, T> mapper, TriConsumer<JsonObject, String, T> saver) {
            this.typeSupplier = typeSupplier;
            this.name = name;
            this.loader = loader;
            this.mapper = mapper;
            this.saver = saver;
            this.pattern = pattern;
        }

        public VarType<?> getType() {
            return typeSupplier.get();
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        public <T> T getValue(JsonObject object) {
            return (T) loader.apply(object, "value");
        }

        public <T> void saveValue(JsonObject object, T value) {
            ((TriConsumer<JsonObject, String, T>) saver).accept(object, "value", value);
        }
    }

    public static @Nullable PrimitiveReference<?>.Instance loadFromString(String string) {
        for (Type type : Type.values()) {
            Matcher matcher = type.pattern.matcher(string);
            if (matcher.matches()) {
                return ModMethods.PRIMITIVE.get().create(type, matcher.group(1));
            }
        }
        return null;
    }

    private PrimitiveReference<?>.Instance create(Type type, String value) {
        return new Instance((T) type.mapper.apply(value), type);
    }
}