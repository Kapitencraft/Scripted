package net.kapitencraft.scripted.code.exe.methods.builtin;

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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WhenMethod<T> extends SpecialMethod<T> {
    public WhenMethod() {
        super(set -> set.addEntry(entry -> entry.addParam("condition", ModVarTypes.BOOL)
                .addWildCardParam("main", "ifFalse")
                .addWildCardParam("main", "ifTrue")
        ));
    }

    @Override
    public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data);
    }

    @Override
    public Method<T>.@Nullable Instance create(String in, VarAnalyser analyser, WildCardData data) { //can remove space before
        int trueStart = in.indexOf('?');
        int falseStart = in.indexOf(':');
        Method<Boolean>.Instance condition = Compiler.compileMethodChain(in.substring(0, trueStart), true, analyser, ModVarTypes.BOOL.get());
        Method<T>.Instance ifTrue = Compiler.compileMethodChain(in.substring(trueStart, falseStart), true, analyser, data.getType("main"));
        Method<T>.Instance ifFalse = Compiler.compileMethodChain(in.substring(falseStart), true, analyser, data.getType("main"));
        if (condition == null || ifTrue == null || ifFalse == null) return null;
        return new Instance(ParamData.create(this.set, List.of(condition, ifTrue, ifFalse), analyser));
    }

    @Override
    public boolean isInstance(String string) {
        return string.contains("?") && string.contains(":");
    }

    public class Instance extends Method<T>.Instance {

        protected Instance(ParamData paramData) {
            super(paramData);
        }

        @Override
        protected T call(VarMap params) {
            if (params.getVarValue("condition", ModVarTypes.BOOL)) {
                return (T) params.getVar("ifTrue").getValue();
            } else {
                return params.hasVar("ifFalse") ? (T) params.getVar("ifFalse").getValue() : null;
            }
        }

        @Override
        public VarType<T> getType(IVarAnalyser analyser) {
            return analyser.getType("ifTrue");
        }
    }
}