package net.kapitencraft.scripted.code.method.elements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.code.MethodPipeline;
import net.kapitencraft.scripted.code.method.MethodCall;
import net.kapitencraft.scripted.code.method.param.ParamBuilder;
import net.kapitencraft.scripted.code.vars.VarMap;

public class WhileLoop extends MethodCall {
    private final Codec<WhileLoopInstance> CODEC = RecordCodecBuilder.create(
            whileLoopInstanceInstance -> whileLoopInstanceInstance.group(
                    Codec.BOOL.fieldOf("condition").forGetter(),
                    MethodPipeline.LOOP_CODEC.fieldOf("body").forGetter()
            ).apply(whileLoopInstanceInstance, WhileLoopInstance::new)
    );
    protected WhileLoop(ParamBuilder builder) {
        super(builder);
    }

    @Override
    public void call(VarMap varMap, MethodPipeline source) {

    }

    @Override
    protected Codec<? extends Instance> getCodec() {
        return CODEC;
    }

    private static class WhileLoopInstance extends Instance {
        private final MethodPipeline pipeline;
        private final boolean condition; //TODO move to supplier system
        private WhileLoopInstance(boolean condition, MethodPipeline pipeline) {
            this.pipeline = pipeline;
            this.condition = condition;
        }
    }
}
