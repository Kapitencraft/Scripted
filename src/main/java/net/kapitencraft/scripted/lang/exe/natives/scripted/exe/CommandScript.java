package net.kapitencraft.scripted.lang.exe.natives.scripted.exe;

import net.kapitencraft.scripted.lang.exe.natives.NativeClass;

@NativeClass(pck = "scripted.exe")
public interface CommandScript {

    void execute(CommandData data);
}
