package net.kapitencraft.scripted.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.CodeInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.JumpInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.BytecodeOptimizer;
import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.SimpleOptimization;
import net.kapitencraft.scripted.lang.exe.Opcode;

/**
 * inlines {@code RETURN} or {@code RETURN_ARG} instructions where they are targeted by {@code JUMP} instructions
 */
public class JumpReturnMergeOptimization implements SimpleOptimization {
    @Override
    public void tryExecute(BytecodeOptimizer.OptimizationStorage optimizationStorage, int index) {
        if (optimizationStorage.getInstruction(index) instanceof JumpInstruction jI && jI.code() == Opcode.JUMP &&
                optimizationStorage.getInstruction(jI.getTarget()) instanceof CodeInstruction cI && (cI.code() == Opcode.RETURN || cI.code() == Opcode.RETURN_ARG)) {
            optimizationStorage.replaceInstruction(index, new CodeInstruction(cI.code()));
        }
    }
}
