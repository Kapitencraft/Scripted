package net.kapitencraft.scripted.code.exe.methods.builtin.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BooleanOperationMethod extends Method<Boolean>{

    public BooleanOperationMethod() {
        super(set -> set.addEntry(entry -> entry
                .addParam("left", VarTypes.BOOL)
                .addParam("right", VarTypes.BOOL)
        ), null);
    }

    @Override
    public Method<Boolean>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data, OperationType.CODEC.byName(GsonHelper.getAsString(object, "operation_type")));
    }

    public Method<?>.Instance create(Method<?>.Instance left, String type, Method<?>.Instance right, VarAnalyser analyser) {
        return new Instance(ParamData.create(set(), analyser, List.of(left, right)), OperationType.CODEC.byName(type));
    }

    public class Instance extends Method<Boolean>.Instance {
        private final OperationType operationType;

        private Instance(ParamData paramData, OperationType operationType) {
            super(paramData);
            this.operationType = operationType;
        }

        @Override
        protected Boolean call(VarMap params, VarMap origin) {
            boolean left = params.getVarValue("left", VarTypes.BOOL);
            boolean right = params.getVarValue("right", VarTypes.BOOL);
            return switch (operationType) {
                case OR -> left || right;
                case AND -> left && right;
                case XOR -> left ^ right;
            };
        }

        @Override
        public VarType<Boolean> getType(IVarAnalyser analyser) {
            return VarTypes.BOOL.get();
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.addProperty("operation_type", operationType.getSerializedName());
        }
    }

    private enum OperationType implements StringRepresentable {
        AND("&&"),
        OR("||"),
        XOR("^");

        public static final EnumCodec<OperationType> CODEC = StringRepresentable.fromEnum(OperationType::values);

        private final String name;

        OperationType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
