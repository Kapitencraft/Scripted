package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

public class ContinueFunction extends Function {

    public ContinueFunction() {
        super(ParamSet.empty(), "continue");
    }

    @Override
    public Instance load(JsonObject object, VarAnalyser analyser) {
        return new Instance();
    }

    @Override
    public Method<Void>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
        return new Instance();
    }

    public class Instance extends Function.Instance {

        protected Instance() {
            super(ParamData.empty());
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            source.setContinued();
        }

        @Override
        public void analyse(VarAnalyser analyser) {
            analyser.setCanceled();
        }
    }
}
