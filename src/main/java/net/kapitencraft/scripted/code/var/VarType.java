package net.kapitencraft.scripted.code.var;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.functions.abstracts.InstanceFunction;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.oop.*;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.ItemStackType;
import net.kapitencraft.scripted.init.custom.ModRegistries;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

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

    public InstanceMethod<?, ?>.Instance buildMethod(JsonObject object, VarAnalyser map, Method<T>.Instance parent) {
        return methods.buildMethod(object, map, parent);
    }

    public InstanceFunction<T>.Instance buildFunction(String type, JsonObject object, VarAnalyser analyser) {
        return this.functions.load(type, object, analyser);
    }

    public Method<T>.Instance buildConstructor(ParamData set) {
        return this.constructor.construct(set);
    }

    public Field<T, ?> getFieldForName(String name) {
        return fields.getOrThrow(name);
    }

    /**
     * adds a new Method to be registered
     * @param name the name of the method. should match the name inside the builder
     * @param builder the builder
     */
    public void addMethod(String name, InstanceMethod<T, ?> builder) {
        this.methods.registerMethod(name, builder);
    }

    public void addField(String name, Field<T, ?> field) {
        this.fields.addField(name, field);
    }

    public void setConstructor(Constructor<T> constructor) {
        if (this.constructor != null) throw new IllegalStateException("can not set constructor twice");
        this.constructor = constructor;
    }

    @Override
    public String toString() {
        return Objects.requireNonNull(ModRegistries.VAR_TYPES.getKey(this)).toString();
    }

    public void addFunction(String name, InstanceFunction<T> function) {
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

    public boolean allowsMul() {
        return mult != null;
    }

    public boolean allowsAdd() {
        return add != null;
    }

    public boolean allowsDiv() {
        return div != null;
    }

    public boolean allowsSub() {
        return sub != null;
    }

    public boolean allowsMod() {
        return mod != null;
    }

    public boolean allowsComparing() {
        return comp != null;
    }
}