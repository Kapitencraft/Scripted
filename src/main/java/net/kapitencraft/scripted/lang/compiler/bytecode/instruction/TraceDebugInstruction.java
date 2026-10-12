package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class TraceDebugInstruction extends CodeInstruction {
    private final byte[] locals;

    public TraceDebugInstruction(byte[] locals) {
        super(Opcode.TRACE);
        this.locals = locals;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addTraceDebug(locals);
    }

    @Override
    public int length() {
        return 3 + locals.length;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
