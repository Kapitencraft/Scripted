package net.kapitencraft.scripted.code.exe;

import net.kapitencraft.scripted.code.var.VarMap;

public interface IExecutable {

    void execute(VarMap map, MethodPipeline<?> pipeline);
}
