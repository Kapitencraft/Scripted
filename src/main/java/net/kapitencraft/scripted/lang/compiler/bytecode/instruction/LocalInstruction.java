package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class LocalInstruction extends CodeInstruction {
    private final int id;

    public LocalInstruction(Opcode opcode, int id) {
        super(opcode);
        this.id = id;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.addArg(id);
    }

    public int getId() {
        return id;
    }

    @Override
    public int length() {
        return 2;
    }

    @Override
    public String toString() {
        return "Local{" + super.toString() + ", ordinal = " + id + "}";
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
