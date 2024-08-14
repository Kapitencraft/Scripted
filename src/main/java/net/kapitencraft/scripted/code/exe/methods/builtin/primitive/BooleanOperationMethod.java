package net.kapitencraft.scripted.code.exe.methods.builtin.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class BooleanOperationMethod extends Method<Boolean>{

    @Override
    public Method<Boolean>.Instance load(JsonObject object, VarAnalyser analyser) {
        Method<Boolean>.Instance left = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "left"), analyser);
        Method<Boolean>.Instance right = JsonHelper.readMethodChain(GsonHelper.getAsJsonObject(object, "right"), analyser);
        return new Instance(OperationType.CODEC.byName(GsonHelper.getAsString(object, "operation_type")), left, right);
    }

    public class Instance extends Method<Boolean>.Instance {
        private final OperationType operationType;
        private final Method<Boolean>.Instance left, right;

        private Instance(OperationType operationType, Method<Boolean>.Instance left, Method<Boolean>.Instance right) {
            this.operationType = operationType;
            this.left = left;
            this.right = right;
        }

        @Override
        public Boolean call(VarMap origin, MethodPipeline<?> pipeline) {
            boolean left = this.left.call(origin, pipeline);
            boolean right = this.right.call(origin, pipeline);
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
