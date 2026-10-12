package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class StringArgInstruction extends CodeInstruction {
    private final String value;

    public StringArgInstruction(Opcode opcode, String value) {
        super(opcode);
        this.value = value;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.injectString(value);
    }

    @Override
    public int length() {
        return 3;
    }

    @Override
    public String toString() {
        return "StringArg{" + super.toString() + ", arg = " + value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
