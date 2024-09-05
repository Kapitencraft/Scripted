package net.kapitencraft.scripted.event.custom;

import net.kapitencraft.scripted.code.exe.methods.builder.BuilderContext;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import org.codehaus.plexus.util.introspection.MethodMap;

public class RegisterExtraMethodsEvent<T> extends Event implements IModBusEvent {

    public final VarType<T> type;

    public RegisterExtraMethodsEvent(VarType<T> type) {
        this.type = type;
    }
}
