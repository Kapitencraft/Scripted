package net.kapitencraft.scripted.code.var;

import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.util.Leveled;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * the variables defined within a MethodPipeline
 * <br> mutable (you can add and modify them using the corresponding methods)
 */
public class VarMap extends Leveled<String, Var<?>> implements IVarAnalyser {

    public void addVarType(String name, VarType<?> varType, boolean isFinal) {
        this.addValue(name, new Var<>(varType, isFinal));
    }

    public <T> void setVar(String name, T val) {
        this.getVar(name).setValue(val);
    }

    public <T> Var<T> getVar(String varName) {
        return (Var<T>) getValue(varName);
    }

    public boolean hasVar(String name) {
        return getValue(name) != null;
    }

    public <T> boolean hasVar(String name, Supplier<? extends VarType<T>> type) {
        return hasVar(name) && getValue(name).getType() == type.get();
    }

    /**
     * @param name the name of the var inside the map
     * @param ignoredType the {@link VarType<T>} the Var should have (will throw exception if not)
     * @return a var containing the Value of that type
     * may throw an exception when no var could be found
     */
    public <T> Var<T> getVar(String name, Supplier<? extends VarType<T>> ignoredType) {
        return getVar(name);
    }

    public <T> T getVarValue(String name, Supplier<? extends VarType<T>> ignoredType) {
        return getVar(name, ignoredType).getValue();
    }

    public <T>Optional<T> getOptionalVarValue(String name, Supplier<? extends VarType<T>> type) {
        if (hasVar(name, type)) return Optional.of(getVarValue(name, type));
        else return Optional.empty();
    }

    @Override
    public <T> VarType<T> getType(String name) {
        return (VarType<T>) this.getVar(name).getType();
    }
}
