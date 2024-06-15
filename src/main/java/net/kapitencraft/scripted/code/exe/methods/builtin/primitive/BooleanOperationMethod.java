package net.kapitencraft.scripted.code.exe.methods.builtin.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.SpecialMethod;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.WildCardData;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.edit.client.text.Compiler;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BooleanOperationMethod extends SpecialMethod<Boolean> {
    private static final Pattern OPERATION = Pattern.compile("((\\|\\|)|(&&)|(\\^))");

    public BooleanOperationMethod() {
        super(set -> set.addEntry(entry -> entry
                .addParam("left", ModVarTypes.BOOL)
                .addParam("right", ModVarTypes.BOOL)
        ));
    }

    @Override
    public Method<Boolean>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data, OperationType.CODEC.byName(GsonHelper.getAsString(object, "operation_type")));
    }

    @Override
    public Method<Boolean>.@Nullable Instance create(String in, VarAnalyser analyser, WildCardData data) { //type is ModVarTypes.BOOL
        Matcher matcher = OPERATION.matcher(in);
        if (matcher.find()) {
            Method<Boolean>.Instance leftCondition = Compiler.compileMethodChain(in.substring(0, matcher.start()), true, analyser, ModVarTypes.BOOL.get());
            Method<Boolean>.Instance rightCondition = Compiler.compileMethodChain(in.substring(matcher.end()), true, analyser, ModVarTypes.BOOL.get());
            OperationType operationType = OperationType.CODEC.byName(matcher.group(1));
            if (leftCondition == null || rightCondition == null || operationType == null) return null; //if either is null we return null
            return new Instance(ParamData.create(this.set, List.of(leftCondition, rightCondition), analyser), operationType);
        }
        return null;
    }

    public class Instance extends Method<Boolean>.Instance {
        private final OperationType operationType;

        private Instance(ParamData paramData, OperationType operationType) {
            super(paramData);
            this.operationType = operationType;
        }

        @Override
        protected Boolean call(VarMap params) {
            boolean left = params.getVarValue("left", ModVarTypes.BOOL);
            boolean right = params.getVarValue("right", ModVarTypes.BOOL);
            return switch (operationType) {
                case OR -> left || right;
                case AND -> left && right;
                case XOR -> left ^ right;
            };
        }

        @Override
        public VarType<Boolean> getType(IVarAnalyser analyser) {
            return ModVarTypes.BOOL.get();
        }

        @Override
        public JsonObject toJson() {
            JsonObject object = super.toJson();
            object.addProperty("operation_type", operationType.getSerializedName());
            return object;
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
