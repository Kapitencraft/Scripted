package net.kapitencraft.scripted.code.exe.methods.builtin.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.SpecialMethod;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Comparators<T> extends SpecialMethod<Boolean> {
    /**
     * <i>don't look</i>
     */
    private static final Pattern COMPARATORS = Pattern.compile("([<>=!]?[=<>])");

    public Comparators() {
        super(set -> set.addEntry(entry -> entry
                .addWildCardParam("left", "right")
                .addWildCardParam("right", "left")
        ));
    }

    @Override
    public Method<Boolean>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data, );
    }

    @Override
    public Method<Boolean>.@Nullable Instance create(String in, VarAnalyser analyser, VarType<Boolean> type) {
        Matcher matcher = COMPARATORS.matcher(in);
        if (matcher.find()) {

        }
        return null;
    }

    public class Instance extends Method<Boolean>.Instance {
        private final CompareMode compareMode;

        protected Instance(ParamData paramData, CompareMode compareMode) {
            super(paramData);
            this.compareMode = compareMode;
        }

        @Override
        protected Boolean call(VarMap params) {
            Var<T> leftVar = params.getVar("left");
            Var<T> rightVar = params.getVar("right");
            T left = leftVar.getValue();
            T right = rightVar.getValue();
            VarType<T> varType = leftVar.getType();
            if (!varType.allowsComparing()) {
                return (this.compareMode == CompareMode.EQUAL) == (left == right);
            }
            double a = varType.compare(left); double b = varType.compare(right);
            return switch (this.compareMode) {
                case EQUAL -> a == b;
                case NEQUAL -> a != b;
                case GEQUAL -> a >= b;
                case LEQUAL -> a <= b;
                case GREATER -> a > b;
                case LESSER -> a < b;
            };
        }

        @Override
        public VarType<Boolean> getType(IVarAnalyser analyser) {
            return ModVarTypes.BOOL.get();
        }
    }

    private enum CompareMode implements StringRepresentable {
        EQUAL("=="),
        NEQUAL("!="),
        GEQUAL(">="),
        LEQUAL("<="),
        GREATER(">"),
        LESSER("<");

        public static final EnumCodec<CompareMode> CODEC = StringRepresentable.fromEnum(CompareMode::values);

        private final String regex;

        CompareMode(String regex) {
            this.regex = regex;
        }

        @Override
        public @NotNull String getSerializedName() {
            return regex;
        }
    }
}
