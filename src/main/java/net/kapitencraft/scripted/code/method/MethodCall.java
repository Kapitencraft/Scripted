package net.kapitencraft.scripted.code.method;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.code.MethodPipeline;
import net.kapitencraft.scripted.code.method.param.ParamBuilder;
import net.kapitencraft.scripted.code.vars.VarMap;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public abstract class MethodCall {
    public static final Codec<MethodCall> CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<MethodCall> read(DynamicOps<T> ops, T input) {
            MethodCall call = ops.getStringValue(input).map(ResourceLocation::new).map(ModRegistries.METHODS::getValue).getOrThrow(false, Scripted.LOGGER::error);
            return null;
        }

        @Override
        public <T> T write(DynamicOps<T> ops, MethodCall value) {
            return null;
        }
    };
    private final ParamBuilder builder;

    protected MethodCall(ParamBuilder builder) {
        this.builder = builder;
    }

    public void callAfterCheck(VarMap varMap, MethodPipeline pipeline) {
        if (builder.hasParams(varMap)) {
            call(varMap, pipeline);
        }
    }

    public abstract void call(VarMap varMap, MethodPipeline source);

    protected static void callMethod(Supplier<? extends MethodCall> call, VarMap map, MethodPipeline source) {
        call.get().callAfterCheck(map, source);
    }

    protected abstract Codec<? extends Instance> getCodec();

    protected static class Instance {

    }
}
