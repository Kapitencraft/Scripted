package net.kapitencraft.scripted.lang.compiler.bytecode.instruction.constant;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.CodeInstruction;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class FloatConstantInstruction extends CodeInstruction {
    private final float value;

    public FloatConstantInstruction(float value) {
        super(Opcode.F_CONST);
        this.value = value;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addFloatConstant(value);
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
