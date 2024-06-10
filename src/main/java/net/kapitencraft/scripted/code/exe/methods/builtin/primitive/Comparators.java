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

public class Comparators<T> extends Method<Boolean> {

    public Comparators() {
        super(ParamSet.single(ParamSet.builder().addWildCardParam("left", "right").addWildCardParam("right", "left")), "comparators");
    }

    @Override
    public Method<Boolean>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data);
    }

    public class Instance extends Method<Boolean>.Instance {
        private Type type;

        protected Instance(ParamData paramData) {
            super(paramData);
        }

        @Override
        protected Var<Boolean> call(VarMap params) {
            Var<T> leftVar = params.getVar("left");
            Var<T> rightVar = params.getVar("right");
            T left = leftVar.getValue();
            T right = rightVar.getValue();
            VarType<T> varType = leftVar.getType();
            if (!varType.allowsComparing()) {
                return new Var<>(ModVarTypes.BOOL.get(), (this.type == Type.EQUAL) == (leftVar == rightVar));
            }
            double a = varType.compare(left); double b = varType.compare(right);
            return new Var<>(ModVarTypes.BOOL.get(), switch (this.type) {
                case EQUAL -> a == b;
                case NEQUAL -> a != b;
                case GEQUAL -> a >= b;
                case LEQUAL -> a <= b;
                case GREATER -> a > b;
                case LESSER -> a < b;
            });
        }

        @Override
        public VarType<Boolean> getType(VarAnalyser analyser) {
            return ModVarTypes.BOOL.get();
        }
    }

    private enum Type {
        EQUAL("equal"),
        NEQUAL("nequal"),
        GEQUAL("gequal"),
        LEQUAL("lequal"),
        GREATER("greater"),
        LESSER("lesser");

        private final String name;

        Type(String name) {
            this.name = name;
        }
    }
}
