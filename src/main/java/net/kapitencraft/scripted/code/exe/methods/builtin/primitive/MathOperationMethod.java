package net.kapitencraft.scripted.code.exe.methods.builtin.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.SpecialMethod;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.WildCardData;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.edit.client.text.Compiler;
import net.kapitencraft.scripted.util.Utils;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathOperationMethod<T> extends SpecialMethod<T> {
    private static final Pattern OPERATION = Pattern.compile("[+\\-*/%]");

    public MathOperationMethod() {
        super(set -> set.addEntry(entry -> entry
                .addWildCardParam("main", "right")
                .addWildCardParam("main", "left")
        ));
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public Method<T>.@Nullable Instance create(String in, VarAnalyser analyser, WildCardData data) {
        Matcher matcher = OPERATION.matcher(in);
        if (matcher.find()) {
            Method<T>.Instance left = Compiler.compileMethodChain(in.substring(0, matcher.start()), true, analyser, data.getType("main"));
            Method<T>.Instance right = Compiler.compileMethodChain(in.substring(matcher.end()), true, analyser, data.getType("main"));
            Operation operation = Operation.CODEC.byName(matcher.group());
            if (Utils.checkAnyNull(left, right, operation)) return null;
            return new Instance(ParamData.create(this.set, List.of(left, right), analyser), operation);
        }
        return null;
    }

    public class Instance extends Method<T>.Instance {
        private Operation operation;

        private Instance(ParamData paramData, Operation operation) {
            super(paramData);
            this.operation = operation;
        }

        @Override
        protected T call(VarMap params) {
            Var<T> left = params.getVar("left");
            Var<T> right = params.getVar("right");
            VarType<T> type = left.getType();
            T a = left.getValue(); T b = right.getValue();
            return switch (this.operation) {
                case ADDITION -> type.add(a, b);
                case DIVISION -> type.divide(a, b);
                case SUBTRACTION -> type.sub(a, b);
                case MULTIPLICATION -> type.multiply(a, b);
                case MODULUS -> type.mod(a, b);
            };
        }

        @Override
        public JsonObject toJson() {
            JsonObject parent = super.toJson();
            parent.addProperty("operation_type", operation.getSerializedName());
            return parent;
        }

        @Override
        public VarType<T> getType(IVarAnalyser analyser) {
            return analyser.getType("left");
        }
    }

    @Override
    public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data, Operation.CODEC.byName(GsonHelper.getAsString(object, "operation_type")));
    }

    enum Operation implements StringRepresentable {
        ADDITION("+"),
        MULTIPLICATION("*"),
        DIVISION("/"),
        SUBTRACTION("-"),
        MODULUS("%");

        public static final EnumCodec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values);

        private final String name;

        Operation(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
