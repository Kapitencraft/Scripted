package net.kapitencraft.scripted.code.exe.methods.builtin.primitive;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

public class SizeCompMethod<T> extends Method<Boolean> {
    private final Type type;

    private SizeCompMethod(Type type) {
        super(ParamSet.single(ParamSet.builder().addWildCardParam("left", "right").addWildCardParam("right", "left")), type.name);
        this.type = type;
    }

    @Override
    public Method<Boolean>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data);
    }

    public class Instance extends Method<Boolean>.Instance {

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
            double a = varType.
            return new Var<>(switch (type) {
                case EQUAL -> ;
                case NEQUAL -> null;
                case GEQUAL -> null;
                case LEQUAL -> null;
                case GREATER -> null;
                case LESSER -> null;
            });
        }

        @Override
        public VarType<Boolean> getType(VarAnalyser analyser) {
            return null;
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
