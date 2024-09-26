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
import net.kapitencraft.scripted.lang.env.core.Environment;
import net.kapitencraft.scripted.lang.func.LoxCallable;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.oop.ClassInstance;
import net.kapitencraft.scripted.lang.oop.NativeClassInstance;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;
import net.kapitencraft.scripted.lang.run.Interpreter;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.fml.ModLoader;
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

public abstract class VarType<T> implements LoxClass {
    /**
     * contains a check for any un-allowed name patterns
     */
    private static final Pattern NAME_BLOCKED = Pattern.compile("(^primtive\\$)|(^\\d)|([\\W&&[^<>,]])");

    //Slave maps
    public static final Map<String, VarType<?>> NAME_MAP = ModRegistries.VAR_TYPES.getSlaveMap(ModCallbacks.VarTypes.NAME_MAP, Map.class);
    public static final Map<Class<?>, VarType<?>> CLASS_MAP = ModRegistries.VAR_TYPES.getSlaveMap(ModCallbacks.VarTypes.CLASS_MAP, Map.class);

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

    private final LoxClass superclass;

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
    public VarType(String name, BiFunction<T, T, T> add, BiFunction<T, T, T> mult, BiFunction<T, T, T> div, BiFunction<T, T, T> sub, BiFunction<T, T, T> mod, Comparator<T> comp, LoxClass superclass) {
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
        this.superclass = superclass;
        this.methods = new MethodMap();
        this.fields = new FieldMap<>();
    }

    public String getName() {
        return this.name;
    }

    public String getRegName() {
        return "L" + Objects.requireNonNull(ModRegistries.VAR_TYPES.getKey(this), "unknown VarType for class " + this.getClass().getCanonicalName()).toString().replaceAll(":", "/") + ";";
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

    public boolean allowsOperation(TokenType type) {
        return switch (type) {
            case SUB, SUB_ASSIGN -> sub != null;
            case DIV, DIV_ASSIGN -> div != null;
            case MOD, MOD_ASSIGN -> mod != null;
            case ADD, ADD_ASSIGN -> add != null;
            case MUL, MUL_ASSIGN -> mult != null;
            default -> false;
        };
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public LoxClass superclass() {
        return superclass;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return null;
    }

    @Override
    public LoxClass getStaticMethodType(String name) {
        return null;
    }

    @Override
    public LoxCallable getStaticMethod(String name) {
        return null;
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return false;
    }

    @Override
    public void callConstructor(Environment environment, Interpreter interpreter, List<Object> args) {
        this.methods.getConstructor().get(0)

    }

    @Override
    public ClassInstance createInst(List<Expr> params, Interpreter interpreter) {
        return new NativeClassInstance<>(this, interpreter, );
    }

    @Override
    public boolean isAbstract() {
        return false;
    }


    //reflective class loading
    public abstract Class<T> getTypeClass();

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

        public List<ReturningNode<?>> getConstructor() {
            return constructor.getBaked();
        }

        //make method have return type to prevent compiler from crying
        private <R> R throwNoMethod(String name) {
            throw new IllegalArgumentException("unknown method with name '" + name + "' in VarType '" + VarType.this.getName() + "'");
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

    //Constructor
    public abstract class Constructor extends Method<T> {

        public abstract MethodInstance<T> construct(JsonObject object, VarAnalyser analyser);
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