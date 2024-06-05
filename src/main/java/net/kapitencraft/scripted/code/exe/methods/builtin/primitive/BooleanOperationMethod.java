package net.kapitencraft.scripted.code.exe.methods.builtin.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class BooleanOperationMethod extends Method<Boolean> {

    public BooleanOperationMethod() {
        super(ParamSet.single(ParamSet.builder().addParam("left", ModVarTypes.BOOL).addParam("right", ModVarTypes.BOOL)), "bool_operation");
    }

    @Override
    public Method<Boolean>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data, Type.CODEC.byName(GsonHelper.getAsString(object, "operation_type")));
    }

    public class Instance extends Method<Boolean>.Instance {
        private Type type;

        private Instance(ParamData paramData, Type type) {
            super(paramData);
            this.type = type;
        }

        @Override
        protected Var<Boolean> call(VarMap params) {
            boolean left = params.getVarValue("left", ModVarTypes.BOOL);
            boolean right = params.getVarValue("right", ModVarTypes.BOOL);
            return new Var<>(switch (type) {
                case OR -> left || right;
                case AND -> left && right;
                case XOR -> left ^ right;
            });
        }

        @Override
        public VarType<Boolean> getType(VarAnalyser analyser) {
            return ModVarTypes.BOOL.get();
        }
    }

    private enum Type implements StringRepresentable {
        AND("and"),
        OR("or"),
        XOR("xor");

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
