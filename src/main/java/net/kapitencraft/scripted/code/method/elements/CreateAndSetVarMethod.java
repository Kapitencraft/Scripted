package net.kapitencraft.scripted.code.method.elements;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.kapitencraft.scripted.code.MethodPipeline;
import net.kapitencraft.scripted.code.method.MethodCall;
import net.kapitencraft.scripted.code.method.param.ParamBuilder;
import net.kapitencraft.scripted.code.vars.VarMap;
import net.kapitencraft.scripted.init.MethodCalls;
import net.kapitencraft.scripted.init.VarTypes;

public class CreateAndSetVarMethod extends MethodCall {
    public CreateAndSetVarMethod() {
        super(new ParamBuilder().addParam("Name", VarTypes.STRING).addParam("Type", VarTypes.TYPE).addParam("Value", VarTypes.WILDCARD));
    }

    @Override
    public void call(VarMap varMap, MethodPipeline source) {
        callMethod(MethodCalls.CREATE_VAR, varMap, source);
        callMethod(MethodCalls.SET_VAR, varMap, source);
    }

    @Override
    protected Codec<Instance> getCodec() {
        return null;
    }
}
