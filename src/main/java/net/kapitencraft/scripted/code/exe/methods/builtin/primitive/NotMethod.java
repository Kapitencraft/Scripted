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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NotMethod extends SpecialMethod<Boolean> {

    public NotMethod() {
        super(set -> set.addEntry(entry -> entry.addParam("val", ModVarTypes.BOOL)));
    }

    @Override
    public Method<Boolean>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance(data);
    }

    @Override
    public Method<Boolean>.@Nullable Instance create(String in, VarAnalyser analyser, WildCardData data) {
        Method<Boolean>.Instance condition = Compiler.compileMethodChain(in.substring(1), true, analyser, ModVarTypes.BOOL.get());
        if (condition == null) return null;
        return new Instance(ParamData.create(this.set, List.of(condition), analyser));
    }

    @Override
    public boolean isInstance(String string) {
        return string.startsWith("!");
    }

    public class Instance extends Method<Boolean>.Instance {

        protected Instance(ParamData paramData) {
            super(paramData);
        }

        @Override
        protected Boolean call(VarMap params) {
            return !params.getVarValue("val", ModVarTypes.BOOL);
        }

        @Override
        public VarType<Boolean> getType(IVarAnalyser analyser) {
            return ModVarTypes.BOOL.get();
        }
    }
}
