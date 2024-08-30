package net.kapitencraft.scripted.code.exe.methods.builder.node;

import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;

import java.util.List;

public interface ReturningNode<R> {

    MethodInstance<R> createInst(List<MethodInstance<?>> params);

    int getParamCount();
}
