package net.kapitencraft.scripted.code.oop;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.List;

public class Script {

    private final String name;

    public Script(String name) {
        this.name = name;
    }

    public <T> void newMethod(String name, VarType<T> retType, List<Pair<VarType<?>, String>> params, MethodPipeline<T> tMethodPipeline) {

    }

    public void setConstructor(List<Pair<VarType<?>, String>> params, MethodPipeline<Void> voidMethodPipeline) {

    }
}
