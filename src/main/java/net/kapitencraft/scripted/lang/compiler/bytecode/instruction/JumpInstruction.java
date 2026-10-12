package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class JumpInstruction extends CodeInstruction implements JumpableInstruction {
    private int index = -1;

    public JumpInstruction(Opcode opcode) {
        super(opcode);
    }

    public void setTarget(int index) {
        this.index = index;
    }

    @Override
    public int getTarget() {
        return index;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.add2bArg(ips.getIp(index)); //index is into instruction array, not bytecode array
    }

    @Override
    public int length() {
        return 3;
    }

    @Override
    public String toString() {
        return "JumpInstruction{" + super.toString() + ", target = " + index + "}";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JumpInstruction ji && ji.code() == this.code() && ji.index == this.index;
    }
}
