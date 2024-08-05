package net.kapitencraft.scripted.code.var.type.abstracts;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.kap_lib.helpers.TextHelper;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.mapper.IVarReference;
import net.kapitencraft.scripted.code.exe.methods.mapper.Setter;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.oop.FieldMap;
import net.kapitencraft.scripted.code.oop.MethodMap;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.ItemStackType;
import net.kapitencraft.scripted.code.var.type.collection.ListType;
import net.kapitencraft.scripted.code.var.type.collection.MapType;
import net.kapitencraft.scripted.code.var.type.collection.MultimapType;
import net.kapitencraft.scripted.init.VarTypes;
import net.kapitencraft.scripted.init.custom.ModCallbacks;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;

public class VarType<T> {
    /**
     * contains a check for any un-allowed name patterns
     */
    private static final Pattern NAME_BLOCKED = Pattern.compile("(^\\d)|([\\W&&[^<>]])");

    public static final Map<String, VarType<?>> NAME_MAP = ModRegistries.VAR_TYPES.getSlaveMap(ModCallbacks.Types.NAME_MAP, Map.class);

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


    /**
     * the name of the Type (how it's referred to in code)
     */
    private final String name;
    /**
     * method storage; to add methods see constructor
     */
    private final MethodMap<T> methods;
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
    protected Constructor constructor;
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
     * override in your own type to add {@link VarType#addMethod(Supplier) methods}, {@link VarType#addField fields} and a {@link VarType#setConstructor(Constructor) constructor}
     * @param name the code name of this type, following java conventions
     * @param add a method to compute two values using addition
     * @param mult similar for multiplication
     * @param div similar for division
     * @param sub similar for subtraction
     * @param mod similar for modulus
     * @param comp method to map a var with this type to double to use comparators like >=, ==, <=
     * @see ItemStackType#ItemStackType() Itemstack#init()
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
        this.methods = new MethodMap<>();
        this.fields = new FieldMap<>();
    }

    public VarType<?>.InstanceMethod<?>.Instance buildMethod(JsonObject object, VarAnalyser map, Method<T>.Instance parent) {
        return !object.has("params") ? createFieldReference(GsonHelper.getAsString(object, "name"), parent) : methods.buildMethod(object, map, parent);
    }

    public Method<?>.Instance buildConstructor(JsonObject object, VarAnalyser analyser) {
        return this.constructor.construct(object, analyser);
    }

    public Field<?> getFieldForName(String name) {
        return fields.getOrThrow(name);
    }

    public InstanceMethod<?> getMethodForName(String name) {
        return methods.getOrThrow(name);
    }

    /**
     * adds a new Method to be registered
     * @param builder the builder
     */
    protected void addMethod(Supplier<InstanceMethod<?>> builder) {
        this.methods.registerMethod(builder);
    }

    protected <J> void addField(String name, java.util.function.Function<T, J> getter, BiConsumer<T, J> setter, Supplier<VarType<J>> typeSupplier) {
        this.fields.addField(name, new Field<>(getter, setter, typeSupplier));
    }

    protected void setConstructor(Constructor constructor) {
        if (this.constructor != null && !extendable) throw new IllegalStateException("can not set constructor twice");
        this.constructor = constructor;
    }

    @Override
    public String toString() {
        return Objects.requireNonNull(ModRegistries.VAR_TYPES.getKey(this)).toString();
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

    public void generate() {
        this.methods.generate();
    }

    //Instance Methods/Functions/Constructors/Fields

    //Methods
    public abstract class InstanceMethod<R> extends Method<R> {

        protected InstanceMethod(Consumer<ParamSet> builder, String name) {
            super(builder, name);
        }

        @Override
        public final InstanceMethod<R>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            throw new JsonSyntaxException("do not load an Instance Method directly");
        }

        /**
         * generate an Instance of this method from data (json)
         */
        public abstract InstanceMethod<R>.Instance load(ParamData data, VarAnalyser analyser, Method<?>.Instance parent, JsonObject other);

        /**
         * generate an Instance of this method from code (text)
         */
        public abstract InstanceMethod<R>.Instance create(ParamData paramData, VarAnalyser analyser, Method<?>.Instance inst);

        public abstract class Instance extends Method<R>.Instance {
            protected final @NotNull Method<T>.Instance parent;

            protected Instance(ParamData paramData, @NotNull Method<T>.Instance parent) {
                super(paramData);
                this.parent = parent;
            }

            @Override
            public R call(VarMap params, VarMap origin) {
                return this.callInit((params1, origin1) -> this.call(params1, parent.callInit(params)), params);
            }

            public abstract R call(VarMap map, T inst);
        }
    }

    protected abstract class SimpleInstanceMethod<R> extends InstanceMethod<R> {

        protected SimpleInstanceMethod(Consumer<ParamSet> builder, String name) {
            super(builder, name);
        }

        protected abstract R call(VarMap map, T inst);
        protected abstract VarType<R> getType(IVarAnalyser analyser);

        @Override
        public InstanceMethod<R>.Instance create(ParamData paramData, VarAnalyser analyser, Method<?>.Instance inst) {
            return new Instance(paramData, (Method<T>.Instance) inst);
        }

        @Override
        public InstanceMethod<R>.Instance load(ParamData data, VarAnalyser analyser, Method<?>.Instance parent, JsonObject other) {
            return new Instance(data, (Method<T>.Instance) parent);
        }

        private class Instance extends InstanceMethod<R>.Instance {

            protected Instance(ParamData paramData, Method<T>.@NotNull Instance parent) {
                super(paramData, parent);
            }

            @Override
            public R call(VarMap map, T inst) {
                return SimpleInstanceMethod.this.call(map, inst);
            }

            @Override
            public VarType<R> getType(IVarAnalyser analyser) {
                return SimpleInstanceMethod.this.getType(analyser);
            }
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

        public FieldReference() {
            super(ParamSet.empty(), "field");
        }

        @Override
        public InstanceMethod<R>.Instance load(ParamData data, VarAnalyser analyser, Method<?>.Instance parent, JsonObject other) {
            return null;
        }

        @Override
        public InstanceMethod<R>.Instance create(ParamData paramData, VarAnalyser analyser, Method<?>.Instance inst) {
            throw new IllegalStateException("do not load a Field Reference directly");
        }

        public Method<R>.@NotNull Instance create(VarType<?>.Field<?> field, Method<?>.Instance parent) {
            return new Instance((Field<R>) field, (Method<T>.Instance) parent);
        }

        public class Instance extends InstanceMethod<R>.Instance implements IVarReference {
            private final Field<R> field;

            protected Instance(Field<R> field, Method<T>.Instance parent) {
                super(null, parent);
                this.field = field;
            }

            @Override
            public R call(VarMap map, T inst) {
                return field.getValue(inst);
            }

            @Override
            public Var<R> buildVar(VarMap parent) {
                return field.crtInst(this.parent.callInit(parent));
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

        /**
         * @param paramSet the set that contains entries for params
         */
        protected InstanceFunction(String name, Consumer<ParamSet> paramSet) {
            super(paramSet, name);
        }

        @Override
        public InstanceMethod<Void>.Instance load(ParamData data, VarAnalyser analyser, Method<?>.Instance parent, JsonObject other) {
            return loadInstance(other, analyser, (Method<T>.Instance) parent);
        }

        public final Instance load(JsonObject object, VarAnalyser analyser) {
            Method<T>.Instance method = Method.loadFromSubObject(object, "supplier", analyser);
            return loadInstance(object, analyser, method);
        }

        public abstract Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<T>.Instance inst);

        public abstract class Instance extends InstanceMethod<Void>.Instance {

            protected Instance(ParamData paramData, Method<T>.Instance parent) {
                super(paramData, parent);
            }

            @Override
            public Void call(VarMap map, T inst) {
                return null;
            }

            @Override
            public VarType<Void> getType(IVarAnalyser analyser) {
                return VarTypes.VOID.get();
            }

            @Override
            public final void execute(VarMap map, MethodPipeline<?> source) {
                executeInstanced(map, source, parent.buildVar(map));
            }

            public abstract void executeInstanced(VarMap map, MethodPipeline<?> source, Var<T> instance);
        }
    }

    protected abstract class SimpleInstanceFunction extends InstanceFunction {

        protected SimpleInstanceFunction(String name, Consumer<ParamSet> paramSet) {
            super(name, paramSet);
        }

        protected abstract void executeInstanced(VarMap map, MethodPipeline<?> source, Var<T> instance);

        @Override
        public InstanceFunction.Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<T>.Instance inst) {
            return new Instance(ParamData.of(object, analyser, this.paramSet), inst);
        }

        @Override
        public VarType<T>.InstanceMethod<Void>.Instance create(ParamData paramData, VarAnalyser analyser, Method<?>.Instance inst) {
            return new Instance(paramData, (Method<T>.Instance) inst);
        }

        private class Instance extends InstanceFunction.Instance {

            protected Instance(ParamData paramData, Method<T>.Instance parent) {
                super(paramData, parent);
            }


            @Override
            public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<T> instance) {
                SimpleInstanceFunction.this.executeInstanced(map, source, instance);
            }
        }
    }

    //Constructor
    public abstract class Constructor extends Method<T> {

        protected Constructor(Consumer<ParamSet> params) {
            super(params, "new" + VarType.this.name);
        }

        public abstract Method<T>.Instance construct(JsonObject object, VarAnalyser analyser);
    }

    protected abstract class SimpleConstructor extends Constructor {

        protected SimpleConstructor(Consumer<ParamSet> params) {
            super(params);
        }

        protected abstract T call(VarMap params);

        protected abstract VarType<T> getType(IVarAnalyser analyser);

        @Override
        public Method<T>.Instance construct(JsonObject object, VarAnalyser analyser) {
            return new Instance(ParamData.of(object, analyser, this.paramSet));
        }

        @Override
        public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return new Instance(data);
        }

        private class Instance extends Method<T>.Instance {

            protected Instance(ParamData paramData) {
                super(paramData);
            }

            @Override
            protected T call(VarMap params, VarMap origin) {
                return SimpleConstructor.this.call(params);
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

        public Comparators() {
            super(set -> set.addEntry(entry -> entry
                    .addParam("right", ()-> VarType.this)
                    .addParam("left", ()-> VarType.this)
            ), null);
        }

        @Override
        public Method<Boolean>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return new Instance(data, CompareMode.CODEC.byName(GsonHelper.getAsString(object, "mode")));
        }

        private class Instance extends Method<Boolean>.Instance {
            private final CompareMode compareMode;

            private Instance(ParamData paramData, CompareMode compareMode) {
                super(paramData);
                this.compareMode = compareMode;
            }

            @Override
            protected Boolean call(VarMap params, VarMap origin) {
                T left = params.getVarValue("left", ()-> VarType.this);
                T right = params.getVarValue("right", ()-> VarType.this);
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
                return VarTypes.BOOL.get();
            }
        }

        //TODO compile Comparators
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

        public MathOperationMethod() {
            super(set -> set.addEntry(entry -> entry
                    .addParam("right", ()-> VarType.this)
                    .addParam("left", ()-> VarType.this)
            ), null);
        }

        Method<T>.Instance create(String operation, Method<T>.Instance left, Method<T>.Instance right, VarAnalyser analyser) {
            return new Instance(ParamData.create(this.paramSet, analyser, List.of(left, right)), Operation.CODEC.byName(operation));
        }

        public class Instance extends Method<T>.Instance {
            private final Operation operation;

            private Instance(ParamData paramData, Operation operation) {
                super(paramData);
                this.operation = operation;
            }

            @Override
            protected T call(VarMap params, VarMap origin) {
                T a = params.getVarValue("left", ()-> VarType.this);
                T b = params.getVarValue("right", ()-> VarType.this);
                VarType<T> type = VarType.this;
                return switch (this.operation) {
                    case ADDITION -> type.add(a, b);
                    case DIVISION -> type.divide(a, b);
                    case SUBTRACTION -> type.sub(a, b);
                    case MULTIPLICATION -> type.multiply(a, b);
                    case MODULUS -> type.mod(a, b);
                };
            }

            @Override
            protected void saveAdditional(JsonObject object) {
                object.addProperty("operation_type", operation.getSerializedName());
            }

            @Override
            public VarType<T> getType(IVarAnalyser analyser) {
                return analyser.getType("left");
            }
        }

        @Override
        public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return new Instance(data, Operation.CODEC.byName(GsonHelper.getAsString(object, "operation_type")));
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
        public WhenMethod() {
            super(set -> set.addEntry(entry -> entry.addParam("condition", VarTypes.BOOL)
                    .addParam("ifTrue", ()-> VarType.this)
                    .addParam("ifFalse", ()-> VarType.this)
            ), null);
        }

        @Override
        public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return new Instance(data);
        }

        public Method<T>.Instance createInst(Method<Boolean>.Instance condition, Method<T>.Instance ifTrue, Method<T>.Instance ifFalse, VarAnalyser analyser) {
            return new Instance(ParamData.create(this.paramSet, analyser, List.of(condition, ifTrue, ifFalse)));
        }

        public class Instance extends Method<T>.Instance {

            protected Instance(ParamData paramData) {
                super(paramData);
            }

            @Override
            protected T call(VarMap params, VarMap origin) {
                String bool = params.getVarValue("condition", VarTypes.BOOL).toString();
                bool = bool.substring(0, 1).toUpperCase() + bool.substring(1);
                String valueKey = "if" + bool;
                return params.getVarValue(valueKey, ()-> VarType.this);
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


        protected SetVarMethod() {
            super(ParamSet.empty(), "%ignored");
        }

        @Override
        public InstanceMethod<T>.Instance load(ParamData data, VarAnalyser analyser, Method<?>.Instance parent, JsonObject other) {
            return create(other, analyser, parent);
        }

        @Override
        public InstanceMethod<T>.Instance create(ParamData paramData, VarAnalyser analyser, Method<?>.Instance inst) {
            throw new IllegalStateException("don't load a Set Var directly");
        }

        private InstanceMethod<T>.Instance create(JsonObject object, VarAnalyser analyser, Method<?>.Instance inst) {
            Method<T>.Instance setter = object.has("setter") ? JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "setter"), analyser) : null;
            return new Instance((Method<T>.Instance) inst, setter, Setter.Type.CODEC.byName(GsonHelper.getAsString(object, "setterType")));
        }

        public Method<T>.Instance create(Method<T>.Instance in, Setter.Type operation, Method<T>.Instance inst) {
            return new Instance(in, inst, operation);
        }

        private class Instance extends InstanceMethod<T>.Instance {
            private final Setter<T> setter;

            protected Instance(Method<T>.Instance parent, Method<T>.Instance setter, Setter.Type type) {
                super(ParamData.empty(), parent);
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
            public T call(VarMap map, T inst) {
                T val = this.setter.createVal(inst, map);
                this.parent.buildVar(map).setValue(val);
                return val;
            }

            @Override
            protected T callInit(java.util.function.BiFunction<VarMap, VarMap, T> callFunc, VarMap parent) {
                return this.call(parent, this.parent.callInit(parent));
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