package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

public interface JumpableInstruction {

    void setTarget(int target);

    int getTarget();
}
