package net.kapitencraft.scripted.code.var.type.abstracts;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.exe.param.ParamSet;
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

public abstract class PrimitiveType<I> extends VarType<I> {
    public static final List<PrimitiveType<?>> PRIMITIVES = ModRegistries.VAR_TYPES.getSlaveMap(ModCallbacks.Types.PRIMITIVES, List.class);

    public PrimitiveType(String name, BiFunction<I, I, I> add, BiFunction<I, I, I> mult, BiFunction<I, I, I> div, BiFunction<I, I, I> sub, BiFunction<I, I, I> mod, Comparator<I> comp) {
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

    public abstract I loadPrimitive(String string);

    public abstract void saveToJson(JsonObject object, I value);

    public abstract I loadFromJson(JsonObject object);

    @Override
    public void setExtendable() {
        throw new IllegalAccessError("Primitives can not be extended");
    }

    public Reference.Instance loadPrimitiveInstance(String string) {
        return ((Reference) this.constructor).create(string);
    }

    //technically a constructor
    public class Reference extends Constructor {

        public Reference() {
            super(ParamSet.empty()); //name ignored;
        }

        @Override
        public Method<I>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return new Instance(PrimitiveType.this.loadFromJson(object));
        }

        @Override
        public Method<I>.Instance construct(JsonObject object, VarAnalyser analyser) {
            return new Instance(PrimitiveType.this.loadFromJson(object));
        }

        public Instance create(String string) {
            return new Instance(PrimitiveType.this.loadPrimitive(string));
        }

        public class Instance extends Method<I>.Instance {
            private final I value;

            private Instance(I value) {
                super(null);
                this.value = value;
            }

            @Override
            protected I call(VarMap params, VarMap origin) {
                return value;
            }

            @Override
            public VarType<I> getType(IVarAnalyser analyser) {
                return PrimitiveType.this;
            }

            @Override
            protected void saveAdditional(JsonObject object) {
                object.addProperty("primitive", JsonHelper.saveType(PrimitiveType.this));
                PrimitiveType.this.saveToJson(object, value);
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
