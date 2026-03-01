package net.kapitencraft.scripted.lang.exe.natives.scripted.lang;

import net.kapitencraft.scripted.lang.exe.natives.NativeClass;

@NativeClass(pck = "scripted.lang")
public class FunctionCallError extends VirtualMachineError {

    public FunctionCallError(String message) {
        super(message);
    }

    public FunctionCallError(String message, Throwable cause) {
        super(message, cause);
    }

    public FunctionCallError(Throwable cause) {
        super(cause);
    }
}
