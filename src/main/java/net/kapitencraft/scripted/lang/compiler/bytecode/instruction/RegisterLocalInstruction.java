package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;

public class RegisterLocalInstruction implements Instruction {
    private final int idx;
    private final ClassReference type;
    private final String name;

    public RegisterLocalInstruction(int idx, ClassReference type, String name) {
        this.idx = idx;
        this.type = type;
        this.name = name;
    }

    public int getIdx() {
        return idx;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addLocal(idx, type, name);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RegisterLocalInstruction rli && rli.idx == this.idx && rli.name.equals(this.name) && rli.type.is(this.type);
    }
}
