package net.kapitencraft.scripted.code.var;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.method.elements.abstracts.InstanceFunction;
import net.kapitencraft.scripted.code.method.param.ParamData;
import net.kapitencraft.scripted.code.oop.*;
import net.kapitencraft.scripted.code.method.Method;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.ItemStackType;

import java.util.function.Function;

public class VarType<T> {
    private final MethodMap<T> methods;
    Function<ParamData, Method<T>.Instance> constructor;
    private final FieldMap<T> fields;
    private final FunctionMap<T> functions;

    /**
     * override in your own type to add {@link VarType#addMethod(String, InstanceMethod) methods}, {@link VarType#addField fields}, {@link VarType#addFunction(String, InstanceFunction) functions} and a {@link VarType#setConstructor(Function) constructor}
     * <br> see {@link ItemStackType#ItemStackType() ItemStackType#init()}  as an example
     */
    public VarType() {
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
        return this.constructor.apply(set);
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

    public void setConstructor(Function<ParamData, Method<T>.Instance> constructor) {
        if (this.constructor != null) throw new IllegalStateException("can not set constructor twice");
        this.constructor = constructor;
    }

    public void addFunction(String name, InstanceFunction<T> function) {
        this.functions.addFunction(name, function);
    }
}