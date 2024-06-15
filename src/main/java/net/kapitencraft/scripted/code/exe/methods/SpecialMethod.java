package net.kapitencraft.scripted.code.exe.methods;

import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.exe.methods.param.WildCardData;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class SpecialMethod<T> extends Method<T> {
    protected SpecialMethod(Consumer<ParamSet> setBuilder) {
        super(setBuilder, "special"); //ignored name
    }

    public abstract @Nullable Method<T>.Instance create(String in, VarAnalyser analyser, WildCardData data);

    @Override
    protected final Method<T>.Instance create(ParamData data, Method<?>.Instance parent) {
        return null;
    }
}