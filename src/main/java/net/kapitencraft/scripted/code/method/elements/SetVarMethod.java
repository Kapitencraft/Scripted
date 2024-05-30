package net.kapitencraft.scripted.code.method.elements;

import net.kapitencraft.scripted.code.MethodPipeline;
import net.kapitencraft.scripted.code.method.MethodCall;
import net.kapitencraft.scripted.code.method.param.ParamBuilder;
import net.kapitencraft.scripted.code.vars.VarMap;
import net.kapitencraft.scripted.code.vars.VarType;
import net.kapitencraft.scripted.init.VarTypes;

public class SetVarMethod extends MethodCall {
    public SetVarMethod() {
        super(new ParamBuilder().addParam("Name", VarTypes.STRING).addParam("Value", VarTypes.WILDCARD));
    }

    @Override
    public void call(VarMap varMap, MethodPipeline source) {
        source.getVars().setVar(varMap.getVar("Name"), varMap.getVar("Value"));
    }
}
