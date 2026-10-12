package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public interface Instruction {

    void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips);

    default int length() {
        return -1; //return -1 for non-code instructions
    }
}
