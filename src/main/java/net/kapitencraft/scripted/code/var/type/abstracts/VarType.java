package net.kapitencraft.scripted.code.var.type.abstracts;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.kap_lib.helpers.TextHelper;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.builder.BuilderContext;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.exe.methods.builder.method.MethodBuilder;
import net.kapitencraft.scripted.code.exe.methods.mapper.IVarReference;
import net.kapitencraft.scripted.code.exe.methods.mapper.Setter;
import net.kapitencraft.scripted.code.oop.FieldMap;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.ItemStackType;
import net.kapitencraft.scripted.code.var.type.collection.ListType;
import net.kapitencraft.scripted.code.var.type.collection.MapType;
import net.kapitencraft.scripted.code.var.type.collection.MultimapType;
import net.kapitencraft.scripted.init.custom.ModCallbacks;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.fml.StartupMessageManager;
import net.minecraftforge.fml.loading.progress.ProgressMeter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class VarType<T> {
    /**
     * contains a check for any un-allowed name patterns
     */
    private static final Pattern NAME_BLOCKED = Pattern.compile("(^\\d)|([\\W&&[^<>]])");

    public static final Map<String, VarType<?>> NAME_MAP = ModRegistries.VAR_TYPES.getSlaveMap(ModCallbacks.VarTypes.NAME_MAP, Map.class);

    public static <T> VarType<T> read(String string) {
        string = string.replaceAll(" ", "");
        if (string.startsWith("List")) {
            return (VarType<T>) read(string.substring(4, string.length() - 1)).listOf();
        } else if (string.startsWith("Map")) {
            return (VarType<T>) readMap(MapType::getOrCache, string.substring(3, string.length() - 1));
        } else if (string.startsWith("Multimap")) {
            return (VarType<T>) readMap(MultimapType::getOrCache, string.substring(8, string.length() - 1));
        }
        return (VarType<T>) VarType.NAME_MAP.get(string);
    }

    private static <T, J ,K> VarType<T> readMap(BiFunction<VarType<J>, VarType<K>, VarType<T>> constructor, String sub) {
        String[] elements = sub.split(",");
        return constructor.apply(read(elements[0]), read(elements[1]));
    }


    private final BuilderContext<T> context = new BuilderContext<>(this);
    /**
     * the name of the Type (how it's referred to in code)
     */
    private final String name;
    /**
     * method storage; to add methods see constructor
     */
    private final MethodMap methods;
    /**
     * whether this Type is extendable and other types depend on it
     * <br>(like PlayerType on EntityType)
     * <br> extendables may not remove their generic type definition
     * <br> nor their 'name' constructor param
     */
    private boolean extendable = false;
    /**
     * the constructor; there can only be one
     */
    protected List<Function<BuilderContext<T>, Returning<T>>> constructor;
    /**
     * fields...
     */
    private final FieldMap<T> fields;
    /**
     * all the mathematical operations that can be applied to this type
     */
    private final BiFunction<T, T, T> add, mult, div, sub, mod;
    /**
     * used to compare different values of the same type using GREATER or LESSER
     * @see Comparators.CompareMode
     */
    private final Comparator<T> comp;

    /**
     * override in your own type to add {@link VarType#addMethod(String, Function) methods}, {@link VarType#addField fields} and a {@link VarType#setConstructor(Function) constructor}
     * @param name the code name of this type, following java conventions
     * @param add a method to compute two values using addition
     * @param mult similar for multiplication
     * @param div similar for division
     * @param sub similar for subtraction
     * @param mod similar for modulus
     * @param comp method to map a var with this type to double to use comparators like >=, ==, <=
     * @see ItemStackType#ItemStackType() ItemstackType#init()
     */
    public VarType(String name, BiFunction<T, T, T> add, BiFunction<T, T, T> mult, BiFunction<T, T, T> div, BiFunction<T, T, T> sub, BiFunction<T, T, T> mod, Comparator<T> comp) {
        if (NAME_BLOCKED.matcher(name).find()) {
            throw new IllegalArgumentException("(^\\d)|(\\W) character in name " + TextHelper.wrapInNameMarkers(name));
        }
        this.name = name;
        this.add = add;
        this.mult = mult;
        this.div = div;
        this.sub = sub;
        this.mod = mod;
        this.comp = comp;
        this.methods = new MethodMap();
        this.fields = new FieldMap<>();
    }

    public VarType<?>.InstanceMethod<?>.Instance buildMethod(JsonObject object, VarAnalyser map, Method<T>.Instance parent) {
        return !object.has("params") ? createFieldReference(GsonHelper.getAsString(object, "name"), parent) : methods.buildMethod(object, map, parent);
    }

    public Field<?> getFieldForName(String name) {
        return fields.getOrThrow(name);
    }



    //adding methods

    public void bakeMethods() {
        this.methods.bakeMethods(StartupMessageManager.addProgressBar("Baking " + this.getName(), this.methods.unbakedMethods.size()));
    }

    private class MethodMap {
        private final HashMap<String, VarType<T>.InstanceMethod<?>> builders = new HashMap<>();
        private final HashMap<String, Function<BuilderContext<T>, InstMapper<T, ?>>> unbakedMethods = new HashMap<>();

        public VarType<?>.InstanceMethod<?>.Instance buildMethod(JsonObject object, VarAnalyser analyser, Method<T>.Instance parent) {
            VarType<T>.InstanceMethod<?> method = getOrThrow(GsonHelper.getAsString(object, "type"));
            VarType<T>.InstanceMethod<?>.Instance instance = method.load(analyser, parent, object);
            if (object.has("then")) return instance.loadChild(object.getAsJsonObject("then"), analyser);
            return instance;
        }

        public void bakeMethods(ProgressMeter progressMeter) {
            progressMeter.setAbsolute(0);
            int i = 0;
            int max = unbakedMethods.size();
            for (Map.Entry<String, Function<BuilderContext<T>, InstMapper<T, ?>>> entry : unbakedMethods.entrySet()) {


                i++;
                progressMeter.setAbsolute(i / max);
            }
            progressMeter.complete();
        }

        public void registerMethod(String name, Function<BuilderContext<T>, InstMapper<T, ?>> method) {
            this.unbakedMethods.put(name, method);
        }

        public VarType<T>.InstanceMethod<?> getOrThrow(String name) {
            return Objects.requireNonNull(builders.get(name), "unknown method " + TextHelper.wrapInNameMarkers(name));
        }
    }

    public InstanceMethod<?> getMethodForName(String name) {
        return methods.getOrThrow(name);
    }

    /**
     * adds a new Method to be registered
     * @param builder the builder
     */
    protected void addMethod(String in, Function<BuilderContext<T>, InstMapper<T, ?>> builder) {
        this.methods.registerMethod(in, builder);
    }

    protected <J> void addField(String name, Function<T, J> getter, BiConsumer<T, J> setter, Supplier<VarType<J>> typeSupplier) {
        this.fields.addField(name, new Field<>(getter, setter, typeSupplier));
    }

    protected void setConstructor(Function<BuilderContext<T>, Returning<T>> constructor) {
        this.constructor.add(constructor);
    }

    @Override
    public String toString() {
        return name;
    }


    public void setExtendable() {
        this.extendable = true;
    }

    //Comparing & math operation

    public boolean matches(VarType<?> otherType) {
        return this == otherType;
    }

    public T multiply(T a, T b) {
        return mult.apply(a, b);
    }

    public T add(T a, T b) {
        return add.apply(a, b);
    }

    public T divide(T a, T b) {
        return div.apply(a, b);
    }

    public T sub(T a, T b) {
        return sub.apply(a, b);
    }

    public T mod(T a, T b) {
        return mod.apply(a, b);
    }

    public int compare(T a, T b) {
        return comp.compare(a, b);
    }

    public boolean allowsComparing() {
        return comp != null;
    }

    //List

    public VarType<List<T>> listOf() {
        return new ListType<>(this);
    }

    public String getName() {
        return this.name;
    }

    //Instance Methods/Functions/Constructors/Fields

    //Methods
    public abstract class InstanceMethod<R> extends Method<R> {

        @Override
        public final InstanceMethod<R>.Instance load(JsonObject object, VarAnalyser analyser) {
            throw new JsonSyntaxException("do not load an Instance Method directly");
        }

        /**
         * generate an Instance of this method from data (json)
         */
        public abstract InstanceMethod<R>.Instance load(VarAnalyser analyser, Method<T>.Instance parent, JsonObject other);

        public abstract class Instance extends Method<R>.Instance {
            protected final @NotNull Method<T>.Instance parent;

            protected Instance(@NotNull Method<T>.Instance parent) {
                this.parent = parent;
            }

            @Override
            public final R call(VarMap origin, MethodPipeline<?> pipeline) {
                return this.call(origin, pipeline, parent.call(origin, pipeline));
            }

            public abstract R call(VarMap map, MethodPipeline<?> pipeline, T inst);
        }
    }

    //Fields
    public class Field<J> {
        private final java.util.function.Function<T, J> getter;
        private final @Nullable BiConsumer<T, J> setter;
        private final Supplier<VarType<J>> type;

        public Field(java.util.function.Function<T, J> getter, @Nullable BiConsumer<T, J> setter, @NotNull Supplier<VarType<J>> type) {
            this.type = type;
            this.getter = getter;
            this.setter = setter;
        }

        public Supplier<VarType<J>> getType() {
            return type;
        }

        public J getValue(T in) {
            if (in == null) throw new NullPointerException("can not read field '" + VarType.this + "." + this + "'");
            return getter.apply(in);
        }

        public void setValue(T in, J value) {
            if (setter == null) throw new IllegalAccessError("can not set value of final field");
            setter.accept(in, value);
        }

        public Var<J> crtInst(T t) {
            return new Instance(t);
        }

        private class Instance extends Var<J> {
            private final T val;

            public Instance(T val) {
                super(Field.this.type.get(), Field.this.setter == null);
                this.val = val;
            }

            @Override
            public void setValue(J value) {
                Field.this.setValue(val, value);
            }

            @Override
            public J getValue() {
                return Field.this.getValue(val);
            }
        }
    }

    private final FieldReference<?> fieldReferenceInst = new FieldReference<>();

    private final class FieldReference<R> extends InstanceMethod<R> {

        public Method<R>.@NotNull Instance create(VarType<?>.Field<?> field, Method<?>.Instance parent) {
            return new Instance((Field<R>) field, (Method<T>.Instance) parent);
        }

        @Override
        public InstanceMethod<R>.Instance load(VarAnalyser analyser, Method<T>.Instance parent, JsonObject other) {
            return null;
        }

        public class Instance extends InstanceMethod<R>.Instance implements IVarReference {
            private final Field<R> field;

            protected Instance(Field<R> field, Method<T>.Instance parent) {
                super(parent);
                this.field = field;
            }

            @Override
            public R call(VarMap map, MethodPipeline<?> pipeline, T inst) {
                return field.getValue(inst);
            }

            @Override
            public Var<R> buildVar(VarMap parent, MethodPipeline<?> pipeline) {
                return field.crtInst(this.parent.call(parent, pipeline));
            }

            @Override
            public VarType<R> getType(IVarAnalyser analyser) {
                return field.getType().get();
            }
        }
    }

    public final <J> InstanceMethod<J>.Instance createFieldReference(String s, Method<?>.Instance varInstance) {
        Field<J> field = (Field<J>) getFieldForName(s);
        return (InstanceMethod<J>.Instance) fieldReferenceInst.create(field, varInstance);
    }

    //Functions
    public abstract class InstanceFunction extends InstanceMethod<Void> {

        @Override
        public InstanceMethod<Void>.Instance load(VarAnalyser analyser, Method<T>.Instance parent, JsonObject other) {
            return loadInstance(other, analyser, parent);
        }

        public abstract Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<T>.Instance inst);

        public abstract class Instance extends InstanceMethod<Void>.Instance {

            protected Instance(Method<T>.Instance parent) {
                super(parent);
            }

            @Override
            public VarType<Void> getType(IVarAnalyser analyser) {
                return net.kapitencraft.scripted.init.VarTypes.VOID.get();
            }

            @Override
            public final Void call(VarMap map, MethodPipeline<?> pipeline, T inst) {
                executeInstanced(map, pipeline, inst);
                return null; //always return null
            }

            protected abstract void executeInstanced(VarMap map, MethodPipeline<?> source, T instance);
        }
    }

    protected abstract class SimpleInstanceFunction extends InstanceFunction {

        protected abstract void executeInstanced(VarMap map, MethodPipeline<?> source, T instance);

        @Override
        public InstanceFunction.Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<T>.Instance inst) {
            return new Instance(inst);
        }

        private class Instance extends InstanceFunction.Instance {

            protected Instance(Method<T>.Instance parent) {
                super(parent);
            }

            @Override
            protected void executeInstanced(VarMap map, MethodPipeline<?> source, T instance) {
                SimpleInstanceFunction.this.executeInstanced(map, source, instance);
            }
        }
    }

    //Constructor
    public abstract class Constructor extends Method<T> {

        public abstract Method<T>.Instance construct(JsonObject object, VarAnalyser analyser);
    }

    protected abstract class SimpleConstructor extends Constructor {

        protected abstract T call(VarMap params);

        protected abstract VarType<T> getType(IVarAnalyser analyser);

        @Override
        public Method<T>.Instance construct(JsonObject object, VarAnalyser analyser) {
            return new Instance();
        }

        private class Instance extends Method<T>.Instance {

            @Override
            public T call(VarMap origin, MethodPipeline<?> pipeline) {
                return SimpleConstructor.this.call(origin);
            }

            @Override
            public VarType<T> getType(IVarAnalyser analyser) {
                return SimpleConstructor.this.getType(analyser);
            }
        }
    }

    //Operations

    //Comparators
    public class Comparators extends Method<Boolean> {

        @Override
        public Method<Boolean>.Instance load(JsonObject object, VarAnalyser analyser) {
            Method<T>.Instance left = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "left"), analyser);
            Method<T>.Instance right = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "right"), analyser);
            return new Instance(left, right, CompareMode.CODEC.byName(GsonHelper.getAsString(object, "mode")));
        }

        private class Instance extends Method<Boolean>.Instance {
            private final Method<T>.Instance left, right;
            private final CompareMode compareMode;

            private Instance(Method<T>.Instance left, Method<T>.Instance right, CompareMode compareMode) {
                this.left = left;
                this.right = right;
                this.compareMode = compareMode;
            }

            @Override
            public Boolean call(VarMap origin, MethodPipeline<?> pipeline) {
                T left = this.left.call(origin, pipeline);
                T right = this.right.call(origin, pipeline);
                if (!VarType.this.allowsComparing()) {
                    return (this.compareMode == CompareMode.EQUAL) == (left == right);
                }
                int result = VarType.this.compare(left, right);
                return switch (this.compareMode) {
                    case EQUAL -> result == 0;
                    case NEQUAL -> result != 0;
                    case GEQUAL -> result >= 0;
                    case LEQUAL -> result <= 0;
                    case GREATER -> result > 0;
                    case LESSER -> result < 0;
                };
            }

            @Override
            public VarType<Boolean> getType(IVarAnalyser analyser) {
                return net.kapitencraft.scripted.init.VarTypes.BOOL.get();
            }
        }

        private enum CompareMode implements StringRepresentable {
            EQUAL("=="),
            NEQUAL("!="),
            GEQUAL(">="),
            LEQUAL("<="),
            GREATER(">"),
            LESSER("<");

            public static final EnumCodec<CompareMode> CODEC = StringRepresentable.fromEnum(CompareMode::values);

            private final String regex;

            CompareMode(String regex) {
                this.regex = regex;
            }

            @Override
            public @NotNull String getSerializedName() {
                return regex;
            }
        }
    }

    //Math Operations
    public class MathOperationMethod extends Method<T> {

        Method<T>.Instance create(String operation, Method<T>.Instance left, Method<T>.Instance right, VarAnalyser analyser) {
            return new Instance(left, right, Operation.CODEC.byName(operation));
        }

        @Override
        public Method<T>.Instance load(JsonObject object, VarAnalyser analyser) {
            Method<T>.Instance left = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "left"), analyser);
            Method<T>.Instance right = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "right"), analyser);
            Operation operation = Operation.CODEC.byName(GsonHelper.getAsString(object, "operation"));
            return new Instance(left, right, operation);
        }

        public class Instance extends Method<T>.Instance {
            private final Method<T>.Instance left, right;
            private final Operation operation;

            private Instance(Method<T>.Instance left, Method<T>.Instance right, Operation operation) {
                this.left = left;
                this.right = right;
                this.operation = operation;
            }

            @Override
            public T call(VarMap origin, MethodPipeline<?> pipeline) {
                T left = this.left.call(origin, pipeline);
                T b = this.right.call(origin, pipeline);
                VarType<T> type = VarType.this;
                return switch (this.operation) {
                    case ADDITION -> type.add(left, b);
                    case DIVISION -> type.divide(left, b);
                    case SUBTRACTION -> type.sub(left, b);
                    case MULTIPLICATION -> type.multiply(left, b);
                    case MODULUS -> type.mod(left, b);
                };
            }

            @Override
            protected void saveAdditional(JsonObject object) {
                object.add("left", left.toJson());
                object.add("right", right.toJson());
                object.addProperty("operation_type", operation.getSerializedName());
            }

            @Override
            public VarType<T> getType(IVarAnalyser analyser) {
                return analyser.getType("left");
            }
        }

        public enum Operation implements StringRepresentable {
            ADDITION("+"),
            MULTIPLICATION("*"),
            DIVISION("/"),
            SUBTRACTION("-"),
            MODULUS("%");

            public static final EnumCodec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values);

            private final String name;

            Operation(String name) {
                this.name = name;
            }

            @Override
            public @NotNull String getSerializedName() {
                return name;
            }
        }
    }

    private final MathOperationMethod mathOperationInst = new MathOperationMethod();

    public Method<T>.Instance createMathOperation(String operation, Method<T>.Instance left, Method<T>.Instance right, VarAnalyser analyser) {
        return mathOperationInst.create(operation, left, right, analyser);
    }


    //when
    public class WhenMethod extends Method<T> {

        @Override
        public Method<T>.Instance load(JsonObject object, VarAnalyser analyser) {
            Method<Boolean>.Instance condition = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "condition"), analyser);
            Method<T>.Instance ifTrue = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "ifTrue"), analyser);
            Method<T>.Instance ifFalse = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "ifFalse"), analyser);
            return new Instance(condition, ifTrue, ifFalse);
        }

        public Method<T>.Instance createInst(Method<Boolean>.Instance condition, Method<T>.Instance ifTrue, Method<T>.Instance ifFalse, VarAnalyser analyser) {
            return new Instance(condition, ifTrue, ifFalse);
        }

        public class Instance extends Method<T>.Instance {
            private final Method<Boolean>.Instance condition;
            private final Method<T>.Instance ifTrue, ifFalse;

            public Instance(Method<Boolean>.Instance condition, Method<T>.Instance ifTrue, Method<T>.Instance ifFalse) {
                this.condition = condition;
                this.ifTrue = ifTrue;
                this.ifFalse = ifFalse;
            }

            @Override
            public T call(VarMap origin, MethodPipeline<?> pipeline) {
                return condition.call(origin, pipeline) ? ifTrue.call(origin, pipeline) : ifFalse.call(origin, pipeline);
            }

            @Override
            public VarType<T> getType(IVarAnalyser analyser) {
                return VarType.this;
            }
        }
    }

    private final WhenMethod whenInst = new WhenMethod();

    public Method<T>.Instance createWhen(Method<Boolean>.Instance condition, Method<T>.Instance ifTrue, Method<T>.Instance ifFalse, VarAnalyser analyser) {
        return whenInst.createInst(condition, ifTrue, ifFalse, analyser);
    }

    //set var method

    private class SetVarMethod extends InstanceMethod<T> {

        private InstanceMethod<T>.Instance create(JsonObject object, VarAnalyser analyser, Method<?>.Instance inst) {
            Method<T>.Instance setter = object.has("setter") ? JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "setter"), analyser) : null;
            return new Instance((Method<T>.Instance) inst, setter, Setter.Type.CODEC.byName(GsonHelper.getAsString(object, "setterType")));
        }

        public Method<T>.Instance create(Method<T>.Instance in, Setter.Type operation, Method<T>.Instance inst) {
            return new Instance(in, inst, operation);
        }

        @Override
        public InstanceMethod<T>.Instance load(VarAnalyser analyser, Method<T>.Instance parent, JsonObject other) {
            return null;
        }

        private class Instance extends InstanceMethod<T>.Instance {
            private final Setter<T> setter;

            protected Instance(Method<T>.Instance parent, Method<T>.Instance setter, Setter.Type type) {
                super(parent);
                if (!(parent instanceof IVarReference)) {
                    throw new IllegalStateException("variable expected");
                }
                this.setter = new Setter<>(type, VarType.this, setter);
            }

            @Override
            protected void saveAdditional(JsonObject object) {
                super.saveAdditional(object);
                object.addProperty("setterType", setter.type().getSerializedName());
                if (setter.setter() != null) object.add("setter", setter.setter().toJson());
            }

            @Override
            public T call(VarMap map, MethodPipeline<?> pipeline, T inst) {
                T val = this.setter.createVal(inst, map);
                this.parent.buildVar(map, pipeline).setValue(val);
                return val;
            }

            @Override
            public VarType<T> getType(IVarAnalyser analyser) {
                return VarType.this;
            }
        }
    }

    public Method<T>.Instance createSetVar(Method<T>.Instance var, Setter.Type type, Method<T>.Instance inst) {
        return setVarInst.create(var, type, inst);
    }

    private final SetVarMethod setVarInst = new SetVarMethod();
    /**
     * @return an instance of exactly -1 (or what matches -1 in that var type)
     */
    public T negOne() {
        return null;
    }

    /**
     * @return an instance of exactly 1 (or what matches 1 in that var type)
     */
    public T one() {
        return null;
    }

}