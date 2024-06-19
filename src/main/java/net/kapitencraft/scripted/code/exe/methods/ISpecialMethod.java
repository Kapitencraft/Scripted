package net.kapitencraft.scripted.code.exe.methods;

import net.kapitencraft.scripted.code.exe.methods.param.WildCardData;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import org.jetbrains.annotations.Nullable;

/**
 * a method that doesn't follow the normal 'name(params)' style
 * <br> must be registered directly to the Methods registry
 */
public interface ISpecialMethod<T> {
    @Nullable Method<T>.Instance create(String in, VarAnalyser analyser, WildCardData data);

    /**
     * @param string the string to check; any function related parts (like <blockquote><pre>
     * 'Item item = '
     * </pre></blockquote> have been removed
     * @return if the string represents an object of this method
     */
    boolean isInstance(String string);
}