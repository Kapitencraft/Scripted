package net.kapitencraft.scripted.code.method.elements.abstracts;

import net.kapitencraft.scripted.code.method.MethodPipeline;
import net.kapitencraft.scripted.code.var.VarMap;

public abstract class AppendFunction<P extends AppendableFunction<P>.AppendableInstance> extends Function {
    public abstract class AppendInstance extends Instance {
    }
}