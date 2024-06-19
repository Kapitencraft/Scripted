package net.kapitencraft.scripted.code.var.type.abstracts;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.Runnable;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.ISpecialMethod;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.mapper.VarReference;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.exe.methods.param.WildCardData;
import net.kapitencraft.scripted.code.oop.FieldMap;
import net.kapitencraft.scripted.code.oop.FunctionMap;
import net.kapitencraft.scripted.code.oop.MethodMap;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.ItemStackType;
import net.kapitencraft.scripted.edit.client.text.Compiler;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.kapitencraft.scripted.init.custom.ModCallbacks;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.kapitencraft.scripted.util.Utils;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VarType<T> {
    public static final Map<String, VarType<?>> NAME_MAP = ModRegistries.VAR_TYPES.getSlaveMap(ModCallbacks.Types.NAME_MAP, Map.class);

    private final String name;
    private final MethodMap<T> methods;
    /**
     * whether this Type is extendable and other types depend on it
     * <br>(like PlayerType on EntityType)
     * <br> extendables may not remove their generic type definition
     * <br> nor their 'name' constructor param
     */
    private boolean extendable = false;
    Constructor constructor;
    private final FieldMap<T> fields;
    private final FunctionMap<T> functions;
    private final BiFunction<T, T, T> add, mult, div, sub, mod;
    private final Comparator<T> comp;

    /**
     * override in your own type to add {@link VarType#addMethod(String, InstanceMethod) methods}, {@link VarType#addField fields}, {@link VarType#addFunction(String, InstanceFunction) functions} and a {@link VarType#setConstructor(Constructor) constructor}
     * <br> see {@link ItemStackType#ItemStackType() ItemStackType#init()}  as an example
     * @param add a method to compute two values using addition
     * @param mult similar for multiplication
     * @param div similar for division
     * @param sub similar for subtraction
     * @param mod similar for modulus
     * @param comp method to map a var with this type to double to use comparators like >=, ==, <=
     */
    public VarType(String name, BiFunction<T, T, T> add, BiFunction<T, T, T> mult, BiFunction<T, T, T> div, BiFunction<T, T, T> sub, BiFunction<T, T, T> mod, Comparator<T> comp) {
        this.name = name;
        this.add = add;
        this.mult = mult;
        this.div = div;
        this.sub = sub;
        this.mod = mod;
        this.comp = comp;
        this.methods = new MethodMap<>();
        this.fields = new FieldMap<>();
        this.functions = new FunctionMap<>();
    }

    public VarType<?>.InstanceMethod<?>.Instance buildMethod(JsonObject object, VarAnalyser map, Method<T>.Instance parent) {
        return methods.buildMethod(object, map, parent);
    }

    public InstanceFunction.Instance buildFunction(String type, JsonObject object, VarAnalyser analyser) {
        return this.functions.load(type, object, analyser);
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

    public @Nullable Runnable.Instance loadFunction(String name, String params, VarAnalyser analyser) {
        InstanceFunction function = functions.getOrTrow(name);
        return function.createFromCode(params, analyser);
    }

    /**
     * adds a new Method to be registered
     * @param name the name of the method. should match the name inside the builder
     * @param builder the builder
     */
    protected void addMethod(String name, InstanceMethod<?> builder) {
        this.methods.registerMethod(name, builder);
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

    public void addFunction(String name, InstanceFunction function) {
        this.functions.addFunction(name, function);
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
        return new ListType();
    }

    public String getName() {
        return this.name;
    }

    /**
     * protected, but do not inherit it's only for tags in registries
     */
    @ApiStatus.Internal
    protected class ListType extends VarType<List<T>> {

        public ListType() {
            super("List<" + VarType.this.getName() + ">", null, null, null, null, null, null);
            this.setConstructor(new NewList());
            this.setExtendable(); //only used
            this.addMethod("get", new GetElement());
            this.addMethod("indexOf", new IndexOfElement());
            this.addMethod("size", new SizeElement());

            this.addFunction("add", new AddElement());
        }

        @Override
        public String toString() {
            return "list." + VarType.this;
        }

        private class NewList extends Constructor {
            //TODO uuuuhrg

            protected NewList() {
                super(ParamSet.empty(), "newList");
            }

            @Override
            public Method<List<T>>.Instance construct(JsonObject object, VarAnalyser analyser) {
                return new Instance(ParamData.of(object, analyser, this.paramSet));
            }

            @Override
            public Method<List<T>>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
                return new Instance(data);
            }

            @Override
            protected Method<List<T>>.Instance create(ParamData data, Method<?>.Instance parent) {
                return new Instance(data);
            }

            private class Instance extends Method<List<T>>.Instance {

                protected Instance(ParamData paramData) {
                    super(paramData);
                }

                @Override
                protected List<T> call(VarMap params) {
                    return new ArrayList<>();
                }

                @Override
                public VarType<List<T>> getType(IVarAnalyser analyser) {
                    return ListType.this;
                }
            }
        }
        private class GetElement extends InstanceMethod<T> {

            protected GetElement() {
                super(set -> set.addEntry(entry -> entry.addParam("id", ModVarTypes.INTEGER)), "get");
            }

            @Override
            public InstanceMethod<T>.Instance load(ParamData data, Method<List<T>>.Instance inst, JsonObject object) {
                return new Instance(data, inst);
            }

            @Override
            protected Method<T>.Instance create(ParamData data, Method<?>.Instance parent) {
                return new Instance(data, (Method<List<T>>.Instance) parent);
            }

            private class Instance extends InstanceMethod<T>.Instance {

                protected Instance(ParamData paramData, Method<List<T>>.@NotNull Instance parent) {
                    super(paramData, parent);
                }

                @Override
                public T call(VarMap map, List<T> inst) {
                    return inst.get(map.getVarValue("id", ModVarTypes.INTEGER));
                }

                @Override
                public VarType<T> getType(IVarAnalyser analyser) {
                    return VarType.this;
                }
            }
        }
        private class AddElement extends InstanceFunction {

            protected AddElement() {
                super(set -> set.addEntry(entry -> entry
                        .addParam("value", ()-> VarType.this)
                ));
            }

            @Override
            public InstanceFunction.Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<List<T>>.Instance inst) {
                Method<List<T>>.Instance instance = Method.loadInstance(object, analyser);
                return new Instance(ParamData.of(object, analyser, this.paramSet), instance);
            }

            @Override
            public Function.Instance createFromCode(String params, VarAnalyser analyser) {
                return null;
            }

            private class Instance extends InstanceFunction.Instance {

                protected Instance(ParamData data, Method<List<T>>.Instance instance) {
                    super(data, instance);
                }

                @Override
                public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<List<T>> instance) {
                    instance.getValue().add(map.getVarValue("value", ()-> VarType.this));
                }
            }
        }
        private class IndexOfElement extends InstanceMethod<Integer> {

            protected IndexOfElement() {
                super(set -> set.addEntry(entry -> entry
                        .addParam("element", ()-> VarType.this)
                ), "indexOf");
            }

            @Override
            public VarType<List<T>>.InstanceMethod<Integer>.Instance load(ParamData data, Method<List<T>>.Instance inst, JsonObject object) {
                return new Instance(data, inst);
            }

            @Override
            protected Method<Integer>.Instance create(ParamData data, Method<?>.Instance parent) {
                return new Instance(data, (Method<List<T>>.Instance) parent);
            }

            private class Instance extends InstanceMethod<Integer>.Instance {

                protected Instance(ParamData paramData, Method<List<T>>.@NotNull Instance parent) {
                    super(paramData, parent);
                }

                @Override
                public Integer call(VarMap map, List<T> inst) {
                    return inst.indexOf(map.getVarValue("element", ()-> VarType.this));
                }

                @Override
                public VarType<Integer> getType(IVarAnalyser analyser) {
                    return ModVarTypes.INTEGER.get();
                }
            }
        }
        private class SizeElement extends InstanceMethod<Integer> {

            protected SizeElement() {
                super(ParamSet.empty(), "size");
            }

            @Override
            public VarType<List<T>>.InstanceMethod<Integer>.Instance load(ParamData data, Method<List<T>>.Instance inst, JsonObject object) {
                return new Instance(inst);
            }

            @Override
            protected Method<Integer>.Instance create(ParamData data, Method<?>.Instance parent) {
                return new Instance((Method<List<T>>.Instance) parent);
            }

            private class Instance extends InstanceMethod<Integer>.Instance {

                protected Instance(Method<List<T>>.@NotNull Instance parent) {
                    super(ParamData.empty(), parent);
                }

                @Override
                public Integer call(VarMap map, List<T> inst) {
                    return inst.size();
                }

                @Override
                public VarType<Integer> getType(IVarAnalyser analyser) {
                    return ModVarTypes.INTEGER.get();
                }
            }
        }
    }

    //Instance Methods/Functions/Constructors/Fields

    //Methods
    public abstract class InstanceMethod<K> extends Method<K> {

        protected InstanceMethod(Consumer<ParamSet> builder, String name) {
            super(builder, name);
        }

        public abstract InstanceMethod<K>.Instance load(ParamData data, Method<T>.Instance inst, JsonObject object);

        @Override
        public Method<K>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            throw new JsonSyntaxException("do not load an Instance Method directly");
        }

        public abstract class Instance extends Method<K>.Instance {
            private final @NotNull Method<T>.Instance parent;

            protected Instance(ParamData paramData, @NotNull Method<T>.Instance parent) {
                super(paramData);
                this.parent = parent;
            }

            @Override
            public K call(VarMap params) {
                return this.callInit(map -> this.call(map, parent.callInit(params)), params);
            }

            public abstract K call(VarMap map, T inst);
        }
    }

    //Fields
    public class Field<J> {
        private final java.util.function.Function<T, J> getter;
        private final BiConsumer<T, J> setter;
        private final Supplier<VarType<J>> type;

        public Field(java.util.function.Function<T, J> getter, BiConsumer<T, J> setter, Supplier<VarType<J>> type) {
            this.getter = getter;
            this.setter = setter;
            this.type = type;
        }


        public VarType<J> getType() {
            return type.get();
        }

        public J getValue(T in) {
            if (in == null) throw new NullPointerException("can not read field '" + VarType.this + "." + this + "'");
            return getter.apply(in);
        }

        public void setValue(T in, J value) {
            if (setter == null) throw new IllegalAccessError("can not set value of final field");
            setter.accept(in, value);
        }
    }

    private final FieldReference<?> fieldReferenceInst = new FieldReference<>();

    private final class FieldReference<R> extends InstanceMethod<R> {

        public FieldReference() {
            super(ParamSet.empty(), "field");
        }

        @Override
        public InstanceMethod<R>.Instance load(ParamData data, Method<T>.Instance inst, JsonObject object) {
            throw new IllegalStateException("do not load a Field Reference directly; use 'References.FIELD.load()' instead");
        }

        public <J> InstanceMethod<?>.Instance load(VarType<J>.Field<?> fieldForName, VarReference<J>.Instance instance) {
            return new Instance((VarType<T>.Field<R>) fieldForName, (VarReference<T>.Instance) instance);
        }

        public Method<R>.@NotNull Instance create(VarType<?>.Field<?> field, VarReference<?>.Instance parent) {
            return new Instance((Field<R>) field, (VarReference<T>.Instance) parent);
        }

        @Override
        protected Method<R>.Instance create(ParamData data, Method<?>.Instance parent) {
            throw new IllegalStateException("do not load a Field Reference directly; use 'References.FIELD.load()' instead");
        }

        public class Instance extends InstanceMethod<R>.Instance {
            private final Field<R> field;

            protected Instance(Field<R> field, VarReference<T>.Instance parent) {
                super(null, parent);
                this.field = field;
            }

            @Override
            public R call(VarMap map, T inst) {
                return field.getValue(inst);
            }

            @Override
            public VarType<R> getType(IVarAnalyser analyser) {
                return field.getType();
            }
        }
    }

    public final <J> Method<J>.Instance createFieldReference(String s, VarReference<?>.Instance varInstance) {
        Field<J> field = (Field<J>) getFieldForName(s);
        return (Method<J>.Instance) fieldReferenceInst.create(field, varInstance);
    }

    //Functions
    public abstract class InstanceFunction extends Function {

        /**
         * @param paramSet the set that contains entries for params
         */
        protected InstanceFunction(Consumer<ParamSet> paramSet) {
            super(paramSet);
        }

        public final Instance load(JsonObject object, VarAnalyser analyser) {
            Method<T>.Instance method = Method.loadFromSubObject(object, "supplier", analyser);
            return loadInstance(object, analyser, method);
        }

        public abstract Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<T>.Instance inst);

        public abstract class Instance extends Function.Instance {
            private final Method<T>.Instance parent;

            protected Instance(ParamData paramData, Method<T>.Instance parent) {
                super(paramData);
                this.parent = parent;
            }

            @Override
            public final void execute(VarMap map, MethodPipeline<?> source) {
                executeInstanced(map, source, parent.buildVar(map));
            }

            public abstract void executeInstanced(VarMap map, MethodPipeline<?> source, Var<T> instance);
        }
    }

    //Constructor
    public abstract class Constructor extends Method<T> {
        private static final Pattern NAME_PATTERN = Pattern.compile("new([A-Z]\\w+)");

        protected Constructor(Consumer<ParamSet> params, String name) {
            super(params, name);
            if (!NAME_PATTERN.matcher(name).matches()) {
                throw new IllegalArgumentException("name '" + name + "' does not match pattern '" + NAME_PATTERN.pattern() + "'");
            }
        }

        public abstract Method<T>.Instance construct(JsonObject object, VarAnalyser analyser);
    }

    //Operations

    //Comparators
    public class Comparators extends Method<Boolean> implements ISpecialMethod<Boolean> {
        /**
         * a simple pattern to compile any possible comparator.<br>
         * one of ==, !=, <=, >=, <, >
         */
        public static final Pattern COMPARATORS = Pattern.compile("^(([<>=!]=)|<|>)$");

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

        @Override
        protected Method<Boolean>.Instance create(ParamData data, Method<?>.Instance parent) {
            return null;
        }

        @Override
        public Method<Boolean>.@Nullable Instance create(String in, VarAnalyser analyser, WildCardData data) {
            Matcher matcher = COMPARATORS.matcher(in);
            if (matcher.find()) {
                Method<?>.Instance left = Compiler.compileMethodChain(in.substring(0, matcher.start()), true, analyser, data.getType("main"));
                Method<?>.Instance right = Compiler.compileMethodChain(in.substring(matcher.end()), true, analyser, data.getType("main"));
                CompareMode mode = CompareMode.CODEC.byName(matcher.group(1));
                if (left == null || right == null || mode == null) return null;
                return new Instance(ParamData.create(this.paramSet, List.of(left, right), analyser), mode);
            }
            return null;
        }

        @Override
        public boolean isInstance(String string) {
            return COMPARATORS.matcher(string).find();
        }

        private class Instance extends Method<Boolean>.Instance {
            private final CompareMode compareMode;

            private Instance(ParamData paramData, CompareMode compareMode) {
                super(paramData);
                this.compareMode = compareMode;
            }

            @Override
            protected Boolean call(VarMap params) {
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
                return ModVarTypes.BOOL.get();
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
    public class MathOperationMethod extends Method<T> implements ISpecialMethod<T> {
        private static final Pattern OPERATION = Pattern.compile("[+\\-*/%]");

        public MathOperationMethod() {
            super(set -> set.addEntry(entry -> entry
                    .addParam("right", ()-> VarType.this)
                    .addParam("left", ()-> VarType.this)
            ), null);
        }

        @SuppressWarnings("DataFlowIssue")
        @Override
        public Method<T>.@Nullable Instance create(String in, VarAnalyser analyser, WildCardData data) {
            Matcher matcher = OPERATION.matcher(in);
            if (matcher.find()) {
                Method<T>.Instance left = Compiler.compileMethodChain(in.substring(0, matcher.start()), true, analyser, data.getType("main"));
                Method<T>.Instance right = Compiler.compileMethodChain(in.substring(matcher.end()), true, analyser, data.getType("main"));
                Operation operation = Operation.CODEC.byName(matcher.group());
                if (Utils.checkAnyNull(left, right, operation)) return null;
                return new Instance(ParamData.create(this.paramSet, List.of(left, right), analyser), operation);
            }
            return null;
        }

        @Override
        public boolean isInstance(String string) {
            return OPERATION.matcher(string).find();
        }

        public class Instance extends Method<T>.Instance {
            private final Operation operation;

            private Instance(ParamData paramData, Operation operation) {
                super(paramData);
                this.operation = operation;
            }

            @Override
            protected T call(VarMap params) {
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
            public JsonObject toJson() {
                JsonObject parent = super.toJson();
                parent.addProperty("operation_type", operation.getSerializedName());
                return parent;
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

        @Override
        protected Method<T>.Instance create(ParamData data, Method<?>.Instance parent) {
            return null;
        }

        enum Operation implements StringRepresentable {
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

    //when
    public class WhenMethod extends Method<T> implements ISpecialMethod<T> {
        public WhenMethod() {
            super(set -> set.addEntry(entry -> entry.addParam("condition", ModVarTypes.BOOL)
                    .addParam("ifTrue", ()-> VarType.this)
                    .addParam("ifFalse", ()-> VarType.this)
            ), null);
        }

        @Override
        public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return new Instance(data);
        }

        @Override
        protected Method<T>.Instance create(ParamData data, Method<?>.Instance parent) {
            return null;
        }

        @Override
        public Method<T>.@Nullable Instance create(String in, VarAnalyser analyser, WildCardData data) { //can remove space before
            int trueStart = in.indexOf('?');
            int falseStart = in.indexOf(':');
            Method<Boolean>.Instance condition = Compiler.compileMethodChain(in.substring(0, trueStart), true, analyser, ModVarTypes.BOOL.get());
            Method<T>.Instance ifTrue = Compiler.compileMethodChain(in.substring(trueStart, falseStart), true, analyser, data.getType("main"));
            Method<T>.Instance ifFalse = Compiler.compileMethodChain(in.substring(falseStart), true, analyser, data.getType("main"));
            if (condition == null || ifTrue == null || ifFalse == null) return null;
            return new Instance(ParamData.create(this.paramSet, List.of(condition, ifTrue, ifFalse), analyser));
        }

        @Override
        public boolean isInstance(String string) {
            return string.contains("?") && string.contains(":");
        }

        public class Instance extends Method<T>.Instance {

            protected Instance(ParamData paramData) {
                super(paramData);
            }

            @Override
            protected T call(VarMap params) {
                String bool = params.getVarValue("condition", ModVarTypes.BOOL).toString();
                bool = bool.substring(0, 1).toUpperCase() + bool.substring(1);
                String valueKey = "if" + bool;
                return params.getVarValue(valueKey, ()-> VarType.this);
            }

            @Override
            public VarType<T> getType(IVarAnalyser analyser) {
                return analyser.getType("ifTrue");
            }
        }
    }
}