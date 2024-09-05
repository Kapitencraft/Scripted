package net.kapitencraft.scripted.code.var.type.abstracts;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.kap_lib.helpers.TextHelper;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.builder.BuilderContext;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.exe.methods.builder.container.ConstructorContainer;
import net.kapitencraft.scripted.code.exe.methods.builder.container.InstanceMethodContainer;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
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
import net.kapitencraft.scripted.event.custom.RegisterExtraMethodsEvent;
import net.kapitencraft.scripted.init.VarTypes;
import net.kapitencraft.scripted.init.custom.ModCallbacks;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.StartupMessageManager;
import net.minecraftforge.fml.loading.progress.ProgressMeter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VarType<T> {
    /**
     * contains a check for any un-allowed name patterns
     */
    private static final Pattern NAME_BLOCKED = Pattern.compile("(^primtive\\$)|(^\\d)|([\\W&&[^<>,]])");

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

    public @NotNull String toId() {
        ResourceLocation location = ModRegistries.VAR_TYPES.getKey(this);
        if (location == null) throw new NullPointerException("VarType " + this.getName() + " has not been registered!");
        return location.toString().replaceAll("[.-]", "/");
    }

    /**
     * the name of the Type (how it's referred to in code)
     */
    private final String name;
    /**
     * method storage; to add methods see constructor
     */
    private final MethodMap methods;
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
     * override in your own type to add {@link VarType#addMethod methods}, {@link VarType#addField fields} and a {@link VarType#addConstructor constructor}
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

    public String getName() {
        return this.name;
    }

    public String getRegName() {
        return "L" + Objects.requireNonNull(ModRegistries.VAR_TYPES.getKey(this), "unknown VarType for class " + this.getClass().getCanonicalName()).toString().replaceAll(":", "/") + ";";
    }


    public MethodInstance<?> buildMethod(JsonObject object, VarAnalyser analyser) {
        return !object.has("params") ? createFieldReference(object, analyser) : methods.readMethod(object, analyser);
    }

    public Field<?> getFieldForName(String name) {
        return fields.getOrThrow(name);
    }



    //adding methods

    /**
     * used to bake the method builders into their dedicated nodes
     * <br> do not call directly!
     */
    @ApiStatus.Internal
    public void bakeMethods() {
        try {
            this.methods.bakeMethods();
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Baking Method " + this.getName());
            throw new ReportedException(report);
        }
    }

    /**
     * used to apply all VarTypes and fetch them out of their RegistryObject state
     * <br> do not call directly!
     */
    @ApiStatus.Internal
    public void createMethods() {
        this.methods.createMethods();
    }

    public void fireExtraMethodsEvent() {
        ModLoader.get().postEvent(new RegisterExtraMethodsEvent<>(this));
    }


    public MethodInstance<?> createMethod(String name, VarAnalyser analyser, List<MethodInstance<?>> methodInstances) {
        return this.methods.createMethodInstance(name, analyser, methodInstances);
    }

    public MethodInstance<T> buildConstructor(JsonObject object, VarAnalyser analyser) {
        return this.methods.readConstructor(object, analyser);
    }

    /**
     * method container for all methods inside this type;
     */
    private class MethodMap {
        /**
         * id pattern for serialized Method ids
         * <br>group 1: VarType name
         * <br>group 2: Method name
         * <br>group 3: Method id
         */
        private static final Pattern SERIALIZED_ID_PATTERN = Pattern.compile("([\\p{Alnum}/_]+)#([\\p{Alnum}/_]+)\\$(\\d+)");

        //must be arraylist to keep index sensitivity
        private final BuilderContext<T> context = new BuilderContext<>(VarType.this);
        private final HashMap<String, InstanceMethodContainer<T>> methods = new HashMap<>();
        private final ConstructorContainer<T> constructor = new ConstructorContainer<>();

        public void createMethods() {
            ProgressMeter progressMeter = StartupMessageManager.addProgressBar("Registering " + VarType.this.getName(), this.methods.size());
            progressMeter.setAbsolute(0);
            this.constructor.create(this.context);
            progressMeter.increment();
            for (Map.Entry<String, InstanceMethodContainer<T>> container : methods.entrySet()) {
                container.getValue().create(this.context, VarType.this.getName(), container.getKey());
                progressMeter.increment();
            }
            progressMeter.complete();
        }

        public void bakeMethods() {
            ProgressMeter progressMeter = StartupMessageManager.addProgressBar("Baking " + VarType.this.getName(), this.methods.size() + 1);
            progressMeter.setAbsolute(0);
            this.constructor.bake(VarType.this.getName(), "Constructor");
            progressMeter.increment();
            for (Map.Entry<String, InstanceMethodContainer<T>> container : methods.entrySet()) {
                container.getValue().bake(VarType.this.getName(), container.getKey());
                progressMeter.increment();
            }
            progressMeter.complete();
        }

        //loading
        private <R> MethodInstance<R> readMethod(JsonObject object, VarAnalyser analyser) {
            String serializedId = GsonHelper.getAsString(object, "type");
            Matcher matcher = SERIALIZED_ID_PATTERN.matcher(serializedId);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("unknown id Pattern: \"" + serializedId + "\"");
            }
            if (!Objects.equals(matcher.group(1), VarType.this.getName())) {
                throw new IllegalAccessError("attempting to get method '" + matcher.group(2) + "' from wrong VarType (expected: '" + matcher.group(1) + "', but got: '" + VarType.this.getName() + "'");
            }
            ReturningNode<R> node = this.methods.get(matcher.group(2)).getByIndex(Integer.parseInt(matcher.group(3)));
            return node.loadInst(object, analyser);
        }

        public MethodInstance<T> readConstructor(JsonObject object, VarAnalyser analyser) {
            String type = GsonHelper.getAsString(object, "type");
            int index = Integer.parseInt(type.split("\\$")[1]);
            return this.constructor.getByIndex(index).loadInst(object, analyser);
        }

        //registering
        public void registerMethod(String name, Function<BuilderContext<T>, InstMapper<T, ?>> method) {
            this.methods.putIfAbsent(name, new InstanceMethodContainer<>());
            this.methods.get(name).register(method);
        }

        public void registerConstructor(Function<BuilderContext<T>, Returning<T>> constructor) {
            this.constructor.register(constructor);
        }

        //using methods
        public Pair<String, ReturningNode<?>> getMethodOrThrow(String name, List<? extends VarType<?>> types) throws IllegalArgumentException {
            if (methods.containsKey(name)) {
                Pair<ReturningNode<?>, Integer> methodAndId = methods.get(name).getMethodAndId(types);
                return Pair.of(VarType.this.getName() + "#" + name + "$" + methodAndId.getSecond(), methodAndId.getFirst());
            } else {
                return throwNoMethod(name);
            }
        }

        public List<ReturningNode<?>> getMethod(String name) {
            if (methods.containsKey(name)) {
                return methods.get(name).getBaked();
            }
            return throwNoMethod(name);
        }

        //using constructors
        public Pair<String, ReturningNode<?>> getConstructorOrThrow(List<? extends VarType<?>> types) throws IllegalArgumentException {
            Pair<ReturningNode<?>, Integer> methodAndId = constructor.getMethodAndId(types);
            return Pair.of("new" + VarType.this.getName() + "$" + methodAndId.getSecond(), methodAndId.getFirst());
        }

        public List<ReturningNode<?>> getConstructor() {
            return constructor.getBaked();
        }

        //make method have return type to prevent compiler from crying
        private <R> R throwNoMethod(String name) {
            throw new IllegalArgumentException("unknown method with name '" + name + "' in VarType '" + VarType.this.getName() + "'");
        }

        //creating
        public MethodInstance<?> createMethodInstance(String name, VarAnalyser analyser, List<MethodInstance<?>> methodInstances) {
            List<? extends VarType<?>> types = methodInstances.stream().map(inst -> inst.getType(analyser)).toList();
            Pair<String, ReturningNode<?>> pair = getMethodOrThrow(name, types);
            if (pair != null) {
                return pair.getSecond().createInst(pair.getFirst(), methodInstances);
            }
            throw new IllegalArgumentException("could not resolve method " + name + "'s signature with " + JsonHelper.getSignature(types));
        }

        public MethodInstance<?> createConstructorInstance(VarAnalyser analyser, List<MethodInstance<?>> methodInstances) {
            List<? extends VarType<?>> types = methodInstances.stream().map(inst -> inst.getType(analyser)).toList();
            Pair<String, ReturningNode<?>> pair = getConstructorOrThrow(methodInstances.stream().map(inst -> inst.getType(analyser)).toList());
            return pair.getSecond().createInst(pair.getFirst(), methodInstances);
        }
    }

    /**
     * adds a new Method to be registered
     * <br> call in child constructor or {@link RegisterExtraMethodsEvent} only
     * @param builder the builder
     */
    public void addMethod(String name, Function<BuilderContext<T>, InstMapper<T, ?>> builder) {
        this.methods.registerMethod(name, builder);
    }

    /**
     * add a field to this Type
     * <br> call in child constructor or {@link RegisterExtraMethodsEvent} only
     * @param name name of the field
     * @param getter getter of the field
     * @param setter setter of the field (might be null if final)
     * @param typeSupplier type of the field
     */
    public <J> void addField(String name, Function<T, J> getter, BiConsumer<T, J> setter, @NotNull Supplier<? extends VarType<J>> typeSupplier) {
        this.fields.addField(name, new Field<>(name, getter, setter, typeSupplier));
    }

    /**
     * add a constructor to this type
     * <br> call in child constructor or {@link RegisterExtraMethodsEvent} only
     * @param constructor the constructor to add
     */
    public void addConstructor(Function<BuilderContext<T>, Returning<T>> constructor) {
        this.methods.registerConstructor(constructor);
    }

    @Override
    public String toString() {
        return name;
    }

    //Comparing & math operation

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
        public abstract InstanceMethod<R>.Instance load(VarAnalyser analyser, MethodInstance<T> parent, JsonObject other);

        public abstract class Instance extends MethodInstance<R> {
            protected final @NotNull MethodInstance<T> parent;

            protected Instance(String id, @NotNull MethodInstance<T> parent) {
                super(id);
                this.parent = parent;
            }

            @Override
            public final R call(VarMap origin, MethodPipeline<?> pipeline) {
                return this.call(origin, pipeline, parent.call(origin, pipeline));
            }

            @Override
            protected void saveAdditional(JsonObject object) {
                object.add("instance", parent.toJson());
                super.saveAdditional(object);
            }

            public abstract R call(VarMap map, MethodPipeline<?> pipeline, T inst);
        }
    }

    //Fields
    public class Field<J> {
        private final String name;
        private final Function<T, J> getter;
        private final @Nullable BiConsumer<T, J> setter;
        private final Supplier<? extends VarType<J>> type;

        public Field(String name, Function<T, J> getter, @Nullable BiConsumer<T, J> setter, @NotNull Supplier<? extends VarType<J>> type) {
            this.name = name;
            this.type = type;
            this.getter = getter;
            this.setter = setter;
        }

        public Supplier<? extends VarType<J>> getType() {
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

        public MethodInstance<R> create(VarType<?>.Field<?> field, MethodInstance<?> parent) {
            return new Instance((Field<R>) field, (MethodInstance<T>) parent);
        }

        @Override
        public InstanceMethod<R>.Instance load(VarAnalyser analyser, MethodInstance<T> parent, JsonObject other) {
            return null;
        }

        public class Instance extends InstanceMethod<R>.Instance implements IVarReference {
            private final Field<R> field;

            protected Instance(Field<R> field, MethodInstance<T> parent) {
                super(VarType.this.getName() + "@" + field.name, parent);
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

    public final <J> InstanceMethod<J>.Instance createFieldReference(JsonObject object, VarAnalyser analyser) {
        return createFieldReference(GsonHelper.getAsString(object, "name"), Method.loadInstance(object, "instance", analyser));
    }

    public final <J> InstanceMethod<J>.Instance createFieldReference(String name, MethodInstance<?> in) {
        return (InstanceMethod<J>.Instance) fieldReferenceInst.create(getFieldForName(name), in);
    }

    //Constructor
    public abstract class Constructor extends Method<T> {

        public abstract MethodInstance<T> construct(JsonObject object, VarAnalyser analyser);
    }

    //Comparators
    public class Comparators extends Method<Boolean> {

        @Override
        public MethodInstance<Boolean> load(JsonObject object, VarAnalyser analyser) {
            MethodInstance<T> left = Method.loadInstance(object, "left", analyser);
            MethodInstance<T> right = Method.loadInstance(object, "right", analyser);
            return new Instance(left, right, CompareMode.CODEC.byName(GsonHelper.getAsString(object, "mode")));
        }

        private class Instance extends MethodInstance<Boolean> {
            private final MethodInstance<T> left, right;
            private final CompareMode compareMode;

            private Instance(MethodInstance<T> left, MethodInstance<T> right, CompareMode compareMode) {
                super("Comp-" + VarType.this.getName());
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
                return VarTypes.BOOL.get();
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

    private final Comparators compsInst = new Comparators();

    public MethodInstance<Boolean> loadComparator(JsonObject object, VarAnalyser analyser) {
        return compsInst.load(object, analyser);
    }

    //Math Operations
    public class MathOperationMethod extends Method<T> {

        MethodInstance<T> create(String operation, MethodInstance<T> left, MethodInstance<T> right) {
            return new Instance(left, right, Operation.CODEC.byName(operation));
        }

        @Override
        public MethodInstance<T> load(JsonObject object, VarAnalyser analyser) {
            MethodInstance<T> left = Method.loadInstance(GsonHelper.getAsJsonObject(object, "left"), analyser);
            MethodInstance<T> right = Method.loadInstance(GsonHelper.getAsJsonObject(object, "right"), analyser);
            Operation operation = Operation.CODEC.byName(GsonHelper.getAsString(object, "operation"));
            return new Instance(left, right, operation);
        }

        public class Instance extends MethodInstance<T> {
            private final MethodInstance<T> left, right;
            private final Operation operation;

            private Instance(MethodInstance<T> left, MethodInstance<T> right, Operation operation) {
                super("Math-" + VarType.this.getName());
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

    public MethodInstance<T> createMathOperation(String operation, MethodInstance<?> left, MethodInstance<?> right) {
        return mathOperationInst.create(operation, (MethodInstance<T>) left, (MethodInstance<T>) right);
    }

    public MethodInstance<T> loadMathOperation(JsonObject object, VarAnalyser analyser) {
        return mathOperationInst.load(object, analyser);
    }

    //when
    public class WhenMethod extends Method<T> {

        @Override
        public MethodInstance<T> load(JsonObject object, VarAnalyser analyser) {
            MethodInstance<Boolean> condition = Method.loadInstance(GsonHelper.getAsJsonObject(object, "condition"), analyser);
            MethodInstance<T> ifTrue = Method.loadInstance(GsonHelper.getAsJsonObject(object, "ifTrue"), analyser);
            MethodInstance<T> ifFalse = Method.loadInstance(GsonHelper.getAsJsonObject(object, "ifFalse"), analyser);
            return new Instance(condition, ifTrue, ifFalse);
        }

        public MethodInstance<T> createInst(MethodInstance<Boolean> condition, MethodInstance<T> ifTrue, MethodInstance<T> ifFalse, VarAnalyser analyser) {
            return new Instance(condition, ifTrue, ifFalse);
        }

        public class Instance extends MethodInstance<T> {
            private final MethodInstance<Boolean> condition;
            private final MethodInstance<T> ifTrue, ifFalse;

            public Instance(MethodInstance<Boolean> condition, MethodInstance<T> ifTrue, MethodInstance<T> ifFalse) {
                super("When-" + VarType.this.getName());
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

    public MethodInstance<T> loadWhen(JsonObject object, VarAnalyser analyser) {
        return whenInst.load(object, analyser);
    }

    public MethodInstance<T> createWhen(MethodInstance<Boolean> condition, MethodInstance<T> ifTrue, MethodInstance<T> ifFalse, VarAnalyser analyser) {
        return whenInst.createInst(condition, ifTrue, ifFalse, analyser);
    }

    //set var method

    private class SetVarMethod extends InstanceMethod<T> {

        private InstanceMethod<T>.Instance create(JsonObject object, VarAnalyser analyser, MethodInstance<?> inst) {
            MethodInstance<T> setter = object.has("setter") ? Method.loadInstance(GsonHelper.getAsJsonObject(object, "setter"), analyser) : null;
            return new Instance((MethodInstance<T>) inst, setter, Setter.Type.CODEC.byName(GsonHelper.getAsString(object, "setterType")));
        }

        public MethodInstance<T> create(MethodInstance<T> in, Setter.Type operation, MethodInstance<T> inst) {
            return new Instance(in, inst, operation);
        }

        @Override
        public InstanceMethod<T>.Instance load(VarAnalyser analyser, MethodInstance<T> parent, JsonObject other) {
            return null;
        }

        private class Instance extends InstanceMethod<T>.Instance {
            private final Setter<T> setter;

            protected Instance(MethodInstance<T> parent, MethodInstance<T> setter, Setter.Type type) {
                super("SetVar-" + VarType.this.getName(), parent);
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
                T val = this.setter.createVal(inst, map, pipeline);
                this.parent.buildVar(map, pipeline).setValue(val);
                return val;
            }

            @Override
            public VarType<T> getType(IVarAnalyser analyser) {
                return VarType.this;
            }
        }
    }

    private final SetVarMethod setVarInst = new SetVarMethod();

    public MethodInstance<T> createSetVar(MethodInstance<T> var, Setter.Type type, MethodInstance<T> inst) {
        return setVarInst.create(var, type, inst);
    }

    public MethodInstance<T> loadSetVar(JsonObject object, VarAnalyser analyser) {
        return setVarInst.load(object, analyser);
    }

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