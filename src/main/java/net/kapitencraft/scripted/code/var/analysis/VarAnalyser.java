package net.kapitencraft.scripted.code.var.analysis;

import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.util.Leveled;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 *
 */
public class VarAnalyser extends Leveled<String, VarType<?>> {
    private boolean canceled;
    private int methodId = 0;
    private final List<Component> errors = new ArrayList<>();

    public void registerVar(String name, VarType<?> varType) {
        this.addValue(name, varType);
    }

    public <T> VarType<T> getVar(String name) {
        return (VarType<T>) this.getType(name);
    }

    public void next() {
        methodId++;
        if (canceled) errors.add(Component.translatable("error.code_after_cancel"));
    }

    public void assertVarExistence(String name, Supplier<? extends VarType<?>> type) {
        if (this.getType(name) != type.get()) errors.add(Component.translatable("error.missing_var", name));
    }

    public void assertVarExistence(String name) {
        if (this.getType(name) != null) errors.add(Component.translatable("error.missing_var", name));
    }

    public void assertOptionVarExistence(String name, Supplier<? extends VarType<?>> type) {
        if (this.getType(name) != null && this.getType(name) != type.get()) errors.add(Component.translatable("error.wrong_var_type", name, this.getType(name), type.get()));
    }

    public void setCanceled() {
        this.canceled = true;
    }

    public void addError(Component component) {
        errors.add(component);
    }
}