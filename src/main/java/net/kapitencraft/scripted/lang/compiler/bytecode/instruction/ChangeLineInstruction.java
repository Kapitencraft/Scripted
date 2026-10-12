package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class ChangeLineInstruction implements Instruction {
    private final int line;

    public ChangeLineInstruction(int line) {
        this.line = line;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.changeLineIfNecessary(line);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ChangeLineInstruction cli && cli.line == this.line;
    }
}
