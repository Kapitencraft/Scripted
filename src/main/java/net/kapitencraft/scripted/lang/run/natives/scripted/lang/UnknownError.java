package net.kapitencraft.scripted.lang.run.natives.scripted.lang;


import net.kapitencraft.scripted.lang.run.natives.NativeClass;

@NativeClass(pck = "scripted.lang")
public class UnknownError extends VirtualMachineError {
    public UnknownError() {
    }

    public UnknownError(String message) {
        super(message);
    }

    public UnknownError(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownError(Throwable cause) {
        super(cause);
    }
}
