package net.kapitencraft.scripted.code.exe.methods;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

public abstract class InstMethod<I, R> extends Method<R> {
    @Override
    public final MethodInstance<R> load(JsonObject object, VarAnalyser analyser) {
        throw new IllegalStateException("don't load instance method directly!");
    }

    public abstract MethodInstance<R> loadInstanced(JsonObject object, VarAnalyser analyser, MethodInstance<I> parent);
}
