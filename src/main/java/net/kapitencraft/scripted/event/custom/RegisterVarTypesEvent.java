package net.kapitencraft.scripted.event.custom;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.function.Supplier;

public class RegisterVarTypesEvent extends Event implements IModBusEvent {
    private final HashMap<Class<?>, VarType<?>> map = new HashMap<>();


    public <T> void registerVarType(Class<T> tClass, Supplier<VarType<T>> type) {
        if (map.containsKey(tClass))
            throw new IllegalArgumentException("tried registering class '" + tClass.getCanonicalName() + "' twice");
        map.put(tClass, type.get());
    }

    @ApiStatus.Internal
    public HashMap<Class<?>, VarType<?>> getContent() {
        return map;
    }
}
