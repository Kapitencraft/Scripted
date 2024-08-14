package net.kapitencraft.scripted.code.exe.methods.builder;

import net.kapitencraft.scripted.code.exe.methods.builder.consumer.CB1P;
import net.kapitencraft.scripted.code.exe.methods.builder.method.MB1P;
import net.kapitencraft.scripted.code.exe.methods.builder.method.MethodBuilder;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.function.Supplier;

public class BuilderContext<I> {
    private final VarType<I> instType;

    public BuilderContext(VarType<I> instType) {
        this.instType = instType;
    }

    public CB1P<I> consumer() {
        return new CB1P<>(ParamInst.of("instance", instType));
    }

    public <R> MB1P<R, I> returning(Supplier<? extends VarType<R>> retType) {
        return returning(retType.get());
    }

    public <R> MB1P<R, I> returning(VarType<R> retType) {
        return new MB1P<>(retType, new ParamInst<>(instType, "instance"));
    }

    public MethodBuilder<I> constructor() {
        return new MethodBuilder<>(instType);
    }
}
