package net.kapitencraft.scripted.lang.compiler.bytecode.instruction.constant;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.CodeInstruction;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class DoubleConstantInstruction extends CodeInstruction {
    private final double value;

    public DoubleConstantInstruction(double value) {
        super(Opcode.D_CONST);
        this.value = value;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addDoubleConstant(value);
    }

    @Override
    public int length() {
        return 3;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        return false; //make sure such objects are never popped
    }
}
