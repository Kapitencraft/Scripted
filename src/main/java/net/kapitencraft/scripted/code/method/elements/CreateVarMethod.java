package net.kapitencraft.scripted.code.method.elements;

import com.mojang.serialization.Codec;
import net.kapitencraft.scripted.code.MethodPipeline;
import net.kapitencraft.scripted.code.method.MethodCall;
import net.kapitencraft.scripted.code.method.param.ParamBuilder;
import net.kapitencraft.scripted.code.vars.VarMap;
import net.kapitencraft.scripted.init.VarTypes;

public class CreateVarMethod extends MethodCall {
    public CreateVarMethod() {
        super(new ParamBuilder().addParam("Name", VarTypes.STRING).addParam("Type", VarTypes.TYPE));
    }

    @Override
    public void call(VarMap varMap, MethodPipeline source) {
        source.getVars().addVar(varMap.getVar("Name"), varMap.getVar("Type"));
    }

    @Override
    protected Codec<Instance> getCodec() {
        return null;
    }
}