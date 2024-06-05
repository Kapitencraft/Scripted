package net.kapitencraft.scripted.code.exe.methods.builtin.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class MathOperationMethod<T> extends Method<T> {
    private final Type type;

    private MathOperationMethod(Type type) {
        super(ParamSet.single(ParamSet.builder().addWildCardParam("left", "right").addWildCardParam("right", "left")), type.getSerializedName());
        this.type = type;
    }

    public static <T> MathOperationMethod<T> add() {
        return new MathOperationMethod<>(Type.ADDITION);
    }
    public static <T> MathOperationMethod<T> mul() {
        return new MathOperationMethod<>(Type.MULTIPLICATION);
    }
    public static <T> MathOperationMethod<T> div() {
        return new MathOperationMethod<>(Type.DIVISION);
    }
    public static <T> MathOperationMethod<T> sub() {
        return new MathOperationMethod<>(Type.SUBTRACTION);
    }

    public class Instance extends Method<T>.Instance {

        protected Instance(ParamData paramData) {
            super(paramData);
        }

        @Override
        protected Var<T> call(VarMap params) {
            Var<T> left = params.getVar("left");
            Var<T> right = params.getVar("right");
            VarType<T> type = left.getType();
            T a = left.getValue(); T b = right.getValue();
            return new Var<>(switch (MathOperationMethod.this.type) {
                case ADDITION -> type.add(a, b);
                case DIVISION -> type.divide(a, b);
                case SUBTRACTION -> type.sub(a, b);
                case MULTIPLICATION -> type.multiply(a, b);
            });
        }

        @Override
        public VarType<T> getType(VarAnalyser analyser) {
            return (VarType<T>) analyser.getType("left");
        }
    }

    @Override
    public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data);
    }

    enum Type implements StringRepresentable {
        ADDITION("add"),
        MULTIPLICATION("mul"),
        DIVISION("div"),
        SUBTRACTION("sub");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
