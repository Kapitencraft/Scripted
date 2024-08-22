package net.kapitencraft.scripted.code.exe.methods.builder.node.method;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

public class MethodNode<R> {
    private final VarType<R> retType;

    public MethodNode(VarType<R> retType) {
        this.retType = retType;
    }


}
