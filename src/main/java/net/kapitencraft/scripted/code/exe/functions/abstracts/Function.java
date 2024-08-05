package net.kapitencraft.scripted.code.exe.functions.abstracts;

import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;

import java.util.function.Consumer;

public abstract class Function extends Method<Void> {

    protected Function(Consumer<ParamSet> setBuilder, String name) {
        super(setBuilder, name);
    }

    public abstract class Instance extends Method<Void>.Instance {
        protected Instance(ParamData paramData) {
            super(paramData);
        }

        @Override
        protected Void call(VarMap params, VarMap origin) {
            return null; //always return null
        }

        @Override
        public final VarType<Void> getType(IVarAnalyser analyser) {
            return VarTypes.VOID.get();
        }
    }
}
