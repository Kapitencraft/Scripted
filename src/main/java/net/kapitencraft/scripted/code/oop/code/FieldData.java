package net.kapitencraft.scripted.code.oop.code;

import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FieldData {
    private final HashMap<String, Field<?>> fields = new HashMap<>();
    private final Map<Field<?>, Method<?>.Instance> appliers = new HashMap<>();

    public FieldData() {
    }

    @ApiStatus.Internal
    public void addField(String name, VarType<?> type, boolean isFinal) {
        fields.put(name, new Field<>(type, isFinal));
    }

    @ApiStatus.Internal
    public <T> void addApplier(Field<T> field, Method<T>.Instance inst) {
        appliers.put(field, inst);
    }

    /**
     * @param selfMap a {@link VarMap} only containing <i>this</i> with a self reference
     */
    @ApiStatus.Internal
    public void checkConstructor(VarMap selfMap) {
        this.appliers.forEach((field, instance) -> applyValue(field, instance, selfMap));
        this.fields.values().stream().filter(Field::isFinal).filter(Field::notApplied).forEach(field -> {
            throw new IllegalStateException("variable has not been initialized");
        });
    }

    @ApiStatus.Internal
    private <T> void applyValue(Field<?> field, Method<?>.Instance inst, VarMap map) {
        ((Field<T>) field).setValue(((Method<T>.Instance) inst).callInit(map));
    }


    public <J> J getFieldValue(String name) {
        return (J) fields.get(name).getValue();
    }

    public <J> void setFieldValue(String name, J j) {
        ((Field<J>) this.fields.get(name)).setValue(j);
    }

    public static class Field<T> extends Var<T> {

        public Field(@NotNull VarType<T> type, boolean isFinal) {
            super(type, isFinal);
        }
    }
}