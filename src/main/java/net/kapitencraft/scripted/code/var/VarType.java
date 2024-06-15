package net.kapitencraft.scripted.code.var;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kapitencraft.scripted.code.exe.IExecutable;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.mapper.VarReference;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.oop.Constructor;
import net.kapitencraft.scripted.code.oop.FieldMap;
import net.kapitencraft.scripted.code.oop.FunctionMap;
import net.kapitencraft.scripted.code.oop.MethodMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.ItemStackType;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.*;

public class VarType<T> {
    private final MethodMap<T> methods;
    Constructor<T> constructor;
    private final FieldMap<T> fields;
    private final FunctionMap<T> functions;
    private final BiFunction<T, T, T> add, mult, div, sub, mod;
    private final ToDoubleFunction<T> comp;

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
    public VarType(BiFunction<T, T, T> add, BiFunction<T, T, T> mult, BiFunction<T, T, T> div, BiFunction<T, T, T> sub, BiFunction<T, T, T> mod, ToDoubleFunction<T> comp) {
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

    public @Nullable IExecutable loadFunction(String name, String params, VarAnalyser analyser) {
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

    protected void setConstructor(Constructor<T> constructor) {
        if (this.constructor != null) throw new IllegalStateException("can not set constructor twice");
        this.constructor = constructor;
    }

    @Override
    public String toString() {
        return Objects.requireNonNull(ModRegistries.VAR_TYPES.getKey(this)).toString();
    }

    public void addFunction(String name, InstanceFunction function) {
        this.functions.addFunction(name, function);
    }

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

    public double compare(T a) {
        return comp.applyAsDouble(a);
    }

    public boolean allowsComparing() {
        return comp != null;
    }


    //List

    public VarType<List<T>> listOf() {
        return new ListType();
    }

    private class ListType extends VarType<List<T>> {

        public ListType() {
            super(null, null, null, null, null, null);
            this.addMethod("get", new GetElement());
            this.addMethod("indexOf", new IndexOfElement());
            this.addFunction("add", new AddElement());
        }

        @Override
        public String toString() {
            return "list." + VarType.this;
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

            @Override
            public InstanceFunction.Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<List<T>>.Instance inst) {
                Method<T>.Instance instance = Method.loadInstance(object, analyser);
                return new Instance(inst, instance);
            }

            @Override
            public Function.Instance createFromCode(String params, VarAnalyser analyser) {
                return null;
            }

            private class Instance extends InstanceFunction.Instance {
                private final Method<T>.Instance instance;

                protected Instance(Method<List<T>>.Instance supplier, Method<T>.Instance instance) {
                    super(supplier);
                    this.instance = instance;
                }

                @Override
                public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<List<T>> instance) {
                    instance.getValue().add(this.instance.callInit(map));
                }
            }
        }
        private class IndexOfElement extends InstanceMethod<Integer> {

            protected IndexOfElement() {
                super(set -> set.addEntry(entry -> entry
                        .addWildCardParam("main", "element")
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
                    return inst.indexOf((T) map.getVar("element").getValue());
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
    public class Field<C> {
        private final java.util.function.Function<T, C> getter;
        private final BiConsumer<T, C> setter;
        private final Supplier<VarType<C>> type;

        public Field(java.util.function.Function<T, C> getter, BiConsumer<T, C> setter, Supplier<VarType<C>> type) {
            this.getter = getter;
            this.setter = setter;
            this.type = type;
        }


        public VarType<C> getType() {
            return type.get();
        }

        public C getValue(T in) {
            if (in == null) throw new NullPointerException("can not read field '" + VarType.this + "." + this + "'");
            return getter.apply(in);
        }

        public void setValue(T in, C value) {
            if (setter == null) throw new IllegalAccessError("can not set value of final field");
            setter.accept(in, value);
        }
    }

    public final class FieldReference<R> extends InstanceMethod<T> {

        public FieldReference() {
            super(ParamSet.empty(), "field");
        }

        @Override
        public InstanceMethod<T>.Instance load(ParamData data, Method<T>.Instance inst, JsonObject object) {
            throw new IllegalStateException("do not load a Field Reference directly; use 'References.FIELD.load()' instead");
        }

        public <J> InstanceMethod<?>.Instance load(VarType<J>.Field<?> fieldForName, VarReference<J>.Instance instance) {
            return new Instance((VarType<T>.Field<R>) fieldForName, (VarReference<T>.Instance) instance);
        }

        public Method<T>.@NotNull Instance create(VarType<?>.Field<?> field, VarReference<?>.Instance parent) {
            return new Instance((VarType<T>.Field<R>) field, (VarReference<T>.Instance) parent);
        }

        @Override
        protected Method<T>.Instance create(ParamData data, Method<?>.Instance parent) {
            return null;
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

    //Functions
    public abstract class InstanceFunction extends Function {

        public final Instance load(JsonObject object, VarAnalyser analyser) {
            Method<T>.Instance method = Method.loadFromSubObject(object, "supplier", analyser);
            return loadInstance(object, analyser, method);
        }

        public abstract Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<T>.Instance inst);

        public abstract class Instance extends Function.Instance {
            private final Method<T>.Instance supplier;

            protected Instance(Method<T>.Instance supplier) {
                this.supplier = supplier;
            }

            @Override
            public final void execute(VarMap map, MethodPipeline<?> source) {
                executeInstanced(map, source,  supplier.buildVar(map));
            }

            public abstract void executeInstanced(VarMap map, MethodPipeline<?> source, Var<T> instance);

            @Override
            public void analyse(VarAnalyser analyser) {
                this.supplier.analyse(analyser);
            }

            @Override
            public void save(JsonObject object) {
                object.add("supplier", supplier.toJson());
            }
        }
    }
}