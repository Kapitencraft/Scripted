package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class StaticFieldAccessInstruction extends CodeInstruction {
    private final String className, fieldName;

    public StaticFieldAccessInstruction(Opcode opcode, String className, String fieldName) {
        super(opcode);
        this.className = className;
        this.fieldName = fieldName;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.injectString(className);
        builder.injectString(fieldName);
    }

    @Override
    public int length() {
        return 5;
    }

    @Override
    public String toString() {
        return "StaticFieldAccess{" + super.toString() + ", className = " + className + ", fieldName = " + fieldName + "}";
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
