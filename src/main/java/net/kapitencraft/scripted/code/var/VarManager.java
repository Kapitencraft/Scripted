package net.kapitencraft.scripted.code.var;

import net.kapitencraft.scripted.event.custom.RegisterVarTypesEvent;
import net.minecraftforge.fml.ModLoader;

import java.util.HashMap;

public class VarManager {
    private final HashMap<Class<?>, VarType<?>> varTypes;

    public static void bootstrap() {} //loads the class and therefore calls the event

    public static final VarManager INSTANCE = new VarManager();

    private static HashMap<Class<?>, VarType<?>> fetchTypes() {
        RegisterVarTypesEvent event = new RegisterVarTypesEvent();
        ModLoader.get().postEvent(event);
        return event.getContent();
    }


    public <T> VarType<T> getType(Class<T> inst) {
        try {
            return (VarType<T>) varTypes.get(inst);
        } catch (Exception e) {
            throw new NullPointerException("no type registered for Class '" + inst.getCanonicalName() + "'");
        }
    }

    public VarManager() {
        varTypes = fetchTypes();
    }
}