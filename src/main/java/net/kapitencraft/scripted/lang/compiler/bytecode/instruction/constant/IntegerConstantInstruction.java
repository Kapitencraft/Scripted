package net.kapitencraft.scripted.lang.compiler.bytecode.instruction.constant;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.CodeInstruction;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class IntegerConstantInstruction extends CodeInstruction {
    private final int value;

    public IntegerConstantInstruction(int value) {
        super(Opcode.I_CONST);
        this.value = value;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addIntConstant(value);
    }

    @Override
    public int length() {
        return 3;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
