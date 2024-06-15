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
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Comparators<T> extends SpecialMethod<Boolean> {
    /**
     * <i>don't look</i>
     */
    private static final Pattern COMPARATORS = Pattern.compile("([<>=!]?[=<>])");

    public Comparators() {
        super(set -> set.addEntry(entry -> entry
                .addWildCardParam("main", "right")
                .addWildCardParam("main", "left")
        ));
    }

    @Override
    public Method<Boolean>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data, CompareMode.CODEC.byName(GsonHelper.getAsString(object, "mode")));
    }

    @Override
    public Method<Boolean>.@Nullable Instance create(String in, VarAnalyser analyser, WildCardData data) {
        Matcher matcher = COMPARATORS.matcher(in);
        if (matcher.find()) {
            Method<?>.Instance left = Compiler.compileMethodChain(in.substring(0, matcher.start()), true, analyser, data.getType("main"));
            Method<?>.Instance right = Compiler.compileMethodChain(in.substring(matcher.end()), true, analyser, data.getType("main"));
            CompareMode mode = CompareMode.CODEC.byName(matcher.group(1));
            if (left == null || right == null || mode == null) return null;
            return new Instance(ParamData.create(this.set, List.of(left, right), analyser), mode);
        }
        return null;
    }

    private class Instance extends Method<Boolean>.Instance {
        private final CompareMode compareMode;

        private Instance(ParamData paramData, CompareMode compareMode) {
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
