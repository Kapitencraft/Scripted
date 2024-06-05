package net.kapitencraft.scripted.code.exe.methods.builtin.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class MathOperationMethod<T> extends Method<T> {

    public MathOperationMethod() {
        super(ParamSet.single(ParamSet.builder().addWildCardParam("left", "right").addWildCardParam("right", "left")), "math_operation");
    }

    public class Instance extends Method<T>.Instance {
        private Type type;

        private Instance(ParamData paramData, Type type) {
            super(paramData);
            this.type = type;
        }

        @Override
        protected Var<T> call(VarMap params) {
            Var<T> left = params.getVar("left");
            Var<T> right = params.getVar("right");
            VarType<T> type = left.getType();
            T a = left.getValue(); T b = right.getValue();
            return new Var<>(switch (this.type) {
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
        return new Instance(data, Type.CODEC.byName(GsonHelper.getAsString(object, "operation_type")));
    }

    enum Type implements StringRepresentable {
        ADDITION("add"),
        MULTIPLICATION("mul"),
        DIVISION("div"),
        SUBTRACTION("sub");

        public static final EnumCodec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

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
