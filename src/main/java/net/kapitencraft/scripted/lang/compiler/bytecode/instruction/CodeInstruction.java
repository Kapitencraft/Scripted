package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class CodeInstruction implements Instruction {
    public static final CodeInstruction POP = new CodeInstruction(Opcode.POP);
    public static final CodeInstruction POP_2 = new CodeInstruction(Opcode.POP_2);
    public static final CodeInstruction DUP = new CodeInstruction(Opcode.DUP);

    private final Opcode opcode;

    public CodeInstruction(Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addCode(opcode);
    }

    @Override
    public int length() {
        return 1;
    }

    public Opcode code() {
        return opcode;
    }

    @Override
    public String toString() {
        return opcode.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CodeInstruction ci && ci.opcode == this.opcode;
    }
}
