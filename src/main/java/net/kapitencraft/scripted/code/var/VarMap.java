package net.kapitencraft.scripted.code.var;

import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.util.Leveled;

import java.util.function.Supplier;

/**
 * the variables defined within a MethodPipeline
 * <br> mutable (you can add and modify them using the corresponding methods)
 */
public class VarMap extends Leveled<String, Var<?>> implements IVarAnalyser {

    public void addVarType(String name, VarType<?> varType, boolean isFinal) {
        this.addValue(name, new Var<>(varType, isFinal));
    }

    /**
     * @param name name of the variable
     * @param val the value to set
     */
    public <T> void setVar(String name, T val) {
        this.getVar(name).setValue(val);
    }

    /**
     * @param varName name of the variable
     * @return the var if it exists, otherwise throws
     * @throws NullPointerException if the var doesn't exist
     */
    public <T> Var<T> getVar(String varName) {
        return (Var<T>) getValue(varName);
    }

    /**
     * @param name name of the variable
     * @return if the VarMap contains a Var for the given name
     */
    public boolean hasVar(String name) {
        return getValue(name) != null;
    }

    /**
     * @param name name of the variable
     * @param type type of the variable
     * @return if the VarMap contains a Var for the given name, with the given Type
     */
    public <T> boolean hasVar(String name, Supplier<? extends VarType<T>> type) {
        return hasVar(name) && getValue(name).getType() == type.get();
    }

    /**
     * @param name the name of the variable
     * @param ignoredType the {@link VarType<T>} the Var should have (will throw exception if not)
     * @return a var containing the Value of that type
     * may throw an exception when no var could be found
     */
    public <T> Var<T> getVar(String name, Supplier<? extends VarType<T>> ignoredType) {
        return getVar(name);
    }

    /**
     * @param name the name of the variable
     * @param ignoredType type of the
     * @return the value of the variable
     */
    public <T> T getVarValue(String name, Supplier<? extends VarType<T>> ignoredType) {
        return getVar(name, ignoredType).getValue();
    }

    /**
     * @param name the name of the variable
     * @return the VarType of this Variable
     */
    @Override
    public <T> VarType<T> getType(String name) {
        return (VarType<T>) this.getVar(name).getType();
    }
}
