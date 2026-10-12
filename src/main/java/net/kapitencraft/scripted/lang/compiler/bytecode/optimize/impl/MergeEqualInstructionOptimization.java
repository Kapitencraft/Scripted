package net.kapitencraft.scripted.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.BytecodeOptimizer;
import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.Optimization;

public class MergeEqualInstructionOptimization implements Optimization {

    @Override
    public void optimize(BytecodeOptimizer.OptimizationStorage instructions) {
        int i = 0;
        while (i + 1 < instructions.size()) {
            if (instructions.getInstruction(i).equals(instructions.getInstruction(i + 1))) {
                instructions.removeInstruction(i);
            } else
                i++;
        }
    }
}
