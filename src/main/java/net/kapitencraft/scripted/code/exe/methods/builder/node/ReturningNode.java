package net.kapitencraft.scripted.code.exe.methods.builder.node;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.List;

public interface ReturningNode<R> {

    MethodInstance<R> createInst(String methodId, List<MethodInstance<?>> params);

    int getParamCount();

    MethodInstance<R> loadInst(JsonObject object, VarAnalyser analyser);

    boolean matchesTypes(List<? extends VarType<?>> types);
}
