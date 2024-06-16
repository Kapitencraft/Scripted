package net.kapitencraft.scripted.code.exe.methods;

import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.exe.methods.param.WildCardData;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * a method that doesn't follow the normal 'name(params)' style
 * <br> must be registered directly to the Methods registry
 */
public abstract class SpecialMethod<T> extends Method<T> {
    protected SpecialMethod(Consumer<ParamSet> setBuilder) {
        super(setBuilder, "special"); //ignored name
    }

    public abstract @Nullable Method<T>.Instance create(String in, VarAnalyser analyser, WildCardData data);

    @Override
    protected final Method<T>.Instance create(ParamData data, Method<?>.Instance parent) {
        return null;
    }

    /**
     * @param string the string to check; any function related parts (like <blockquote><pre>
     * 'Item item = '
     * </pre></blockquote> have been removed
     * @return if the string represents an object of this method
     */
    public abstract boolean isInstance(String string);
}