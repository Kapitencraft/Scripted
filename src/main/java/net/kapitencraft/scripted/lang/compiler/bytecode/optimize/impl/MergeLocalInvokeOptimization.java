package net.kapitencraft.scripted.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.CodeInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.LocalInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.RegisterLocalInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.BytecodeOptimizer;
import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.SimpleOptimization;
import net.kapitencraft.scripted.lang.exe.Opcode;

/**
 * finds variable assignments directly followed by access to the same variable, replacing the access with a duplication
 */
public class MergeLocalInvokeOptimization implements SimpleOptimization {
    @Override
    public void tryExecute(BytecodeOptimizer.OptimizationStorage optimizationStorage, int index) {
        if (
                optimizationStorage.size() > index + 1 &&
                        optimizationStorage.getInstruction(index) instanceof RegisterLocalInstruction rLI &&
                        optimizationStorage.getInstruction(index + 1) instanceof LocalInstruction lI &&
                        lI.code() == Opcode.GET &&
                        lI.getId() == rLI.getIdx()
        ) {
            optimizationStorage.replaceInstruction(index + 1, CodeInstruction.DUP);
        }
    }
}
