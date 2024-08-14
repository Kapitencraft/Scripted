package net.kapitencraft.scripted.event.custom;

import net.kapitencraft.scripted.code.exe.methods.builder.BuilderContext;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

public record RegisterExtraMethodsEvent<T>(VarType<T> type, BuilderContext<T> context) {

    public void registerConsumer() {

    }

    public void registerReturning() {

    }
}
