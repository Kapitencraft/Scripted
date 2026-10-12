package net.kapitencraft.scripted.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.JumpInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.BytecodeOptimizer;
import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.SimpleOptimization;
import net.kapitencraft.scripted.lang.exe.Opcode;

/**
 * merges {@code JUMP} instructions targeting other JUMP instructions
 */
public class JumpMergeOptimization implements SimpleOptimization {
    @Override
    public void tryExecute(BytecodeOptimizer.OptimizationStorage optimizationStorage, int index) {
        if (optimizationStorage.getInstruction(index) instanceof JumpInstruction jI &&
                jI.code() == Opcode.JUMP &&
                optimizationStorage.getInstruction(jI.getTarget()) instanceof JumpInstruction jI1 &&
                jI1.code() == Opcode.JUMP
        ) {
            jI.setTarget(jI1.getTarget());
        }
    }
}
