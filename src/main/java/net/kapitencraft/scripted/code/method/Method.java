package net.kapitencraft.scripted.code.method;

import net.kapitencraft.scripted.code.MethodPipeline;
import net.kapitencraft.scripted.code.vars.Var;
import net.kapitencraft.scripted.code.vars.VarMap;

public class Method {
    private final MethodPipeline pipeline;

    public Method(MethodPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public Var<?> call(VarMap in) {
        return this.pipeline.execute(in);
    }
}
