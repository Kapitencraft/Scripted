package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class IncrementIntVarInstruction extends CodeInstruction {
    private final int ordinal, val;

    public IncrementIntVarInstruction(int ordinal, int i) {
        super(Opcode.IIRC);
        this.ordinal = ordinal;
        this.val = i;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.addArg(ordinal);
        builder.addArg(val);
    }

    @Override
    public int length() {
        return 3;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
