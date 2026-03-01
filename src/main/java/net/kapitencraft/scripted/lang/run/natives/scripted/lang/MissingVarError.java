package net.kapitencraft.scripted.lang.run.natives.scripted.lang;

import net.kapitencraft.scripted.lang.run.natives.NativeClass;

@NativeClass(pck = "scripted.lang")
public class MissingVarError extends VirtualMachineError {

    public MissingVarError(String message) {
        super(message);
    }

    public MissingVarError(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingVarError(Throwable cause) {
        super(cause);
    }
}
