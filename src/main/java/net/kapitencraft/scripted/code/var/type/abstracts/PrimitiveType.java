package net.kapitencraft.scripted.code.var.type.abstracts;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.ISpecialMethod;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.exe.methods.param.WildCardData;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.custom.ModCallbacks;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.kapitencraft.scripted.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PrimitiveType<T> extends VarType<T> {
    public static final List<PrimitiveType<?>> PRIMITIVES = ModRegistries.VAR_TYPES.getSlaveMap(ModCallbacks.Types.PRIMITIVES, List.class);

    public PrimitiveType(String name, BiFunction<T, T, T> add, BiFunction<T, T, T> mult, BiFunction<T, T, T> div, BiFunction<T, T, T> sub, BiFunction<T, T, T> mod, Comparator<T> comp) {
        super(name, add, mult, div, sub, mod, comp);
        this.setConstructor(new Reference());
    }

    public static final Pattern NUMBER = Pattern.compile("(\\d+)");
    public static final Pattern RESOURCE_LOCATION_MATCHER = Pattern.compile("(([a-z0-9._-]):?[a-z0-9._-])");

    /**
     * @return the matcher to read an instance of this type from string <br>
     * (must contain exactly one capturing group)
     */
    public abstract Pattern matcher();

    public abstract T loadPrimitive(String string);

    public String openRegex() {
        return "[(, ?:]";
    }
    public String closeRegex() {
        return "[), ?:]";
    }

    public abstract void saveToJson(JsonObject object, T value);
    public abstract T loadFromJson(JsonObject object);

    @Override
    public void setExtendable() {
        throw new IllegalAccessError("Primitives can not be extended");
    }

    public Reference loadPrimitiveInstance(String string) {
        return this.constructor.load()
    }

    //technically a constructor
    public class Reference extends Constructor implements ISpecialMethod<T> {

        public Reference() {
            super(ParamSet.empty(), "primitive"); //name ignored;
        }

        @Override
        public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return new Instance(PrimitiveType.this.loadFromJson(object));
        }

        @Override
        protected Method<T>.Instance create(ParamData data, Method<?>.Instance parent) {
            return null; //primitives aren't loaded via Param reference
        }

        @Override
        public Method<T>.Instance construct(JsonObject object, VarAnalyser analyser) {
            return new Instance(PrimitiveType.this.loadFromJson(object));
        }

        @Override
        public Method<T>.@Nullable Instance create(String in, VarAnalyser analyser, WildCardData data) {
            return create(in);
        }

        public Instance create(String string) {
            return new Instance(PrimitiveType.this.loadPrimitive(string));
        }

        @Override
        public boolean isInstance(String string) {
            return PrimitiveType.this.matcher().matcher(string).find();
        }

        public class Instance extends Method<T>.Instance {
            private final T value;

            private Instance(T value) {
                super(null);
                this.value = value;
            }

            @Override
            protected T call(VarMap params) {
                return value;
            }

            @Override
            public VarType<T> getType(IVarAnalyser analyser) {
                return PrimitiveType.this;
            }

            @Override
            public JsonObject toJson() {
                JsonObject object = new JsonObject();
                object.addProperty("primitive", JsonHelper.saveType(PrimitiveType.this));
                PrimitiveType.this.saveToJson(object, value);
                return object;
            }
        }
    }

    public static <T> PrimitiveType<T>.Reference.@Nullable Instance loadReferenceFromString(String string, PrimitiveType<T> type) {
        Matcher matcher = type.matcher().matcher(string);
        if (matcher.matches()) {
            PrimitiveType<T>.Reference reference = (PrimitiveType<T>.Reference) type.constructor;
            return reference.create(matcher.group(1));
        }
        return null;
    }
}
